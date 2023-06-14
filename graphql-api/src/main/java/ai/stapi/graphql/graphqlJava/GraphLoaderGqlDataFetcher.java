package ai.stapi.graphql.graphqlJava;

import ai.stapi.graphoperations.graphLanguage.graphDescription.GraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.EdgeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.UuidIdentityDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.AttributeQueryDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.CollectionComparisonOperator;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.NodeQueryGraphDescription;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.OutgoingEdgeQueryDescription;
import ai.stapi.graphoperations.graphLoader.GraphLoader;
import ai.stapi.graphoperations.graphLoader.search.SearchQueryParameters;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AllMatchFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.FilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.NoneMatchFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.NotFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.factory.FilterOptionFactory;
import ai.stapi.graphoperations.graphLoader.search.paginationOption.OffsetPaginationOption;
import ai.stapi.graphoperations.graphLoader.search.sortOption.AscendingSortOption;
import ai.stapi.graphoperations.graphLoader.search.sortOption.DescendingSortOption;
import ai.stapi.graphoperations.graphLoader.search.sortOption.SortOption;
import ai.stapi.graphql.graphqlJava.exceptions.CannotLoadRequestedDataByGraphQL;
import ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator.GraphQlFilterInputGenerator;
import ai.stapi.identity.UniqueIdentifier;
import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class GraphLoaderGqlDataFetcher implements DataFetcher<Object> {

  public static final OffsetPaginationOption DEFAULT_MAIN_PAGINATION = new OffsetPaginationOption(0, 10);
  public static final OffsetPaginationOption DEFAULT_INNER_PAGINATION = new OffsetPaginationOption(0, 100);
  private final GraphLoader graphLoader;
  private final FilterOptionFactory filterOptionFactory;

  public GraphLoaderGqlDataFetcher(
      GraphLoader graphLoader,
      FilterOptionFactory filterOptionFactory
  ) {
    this.graphLoader = graphLoader;
    this.filterOptionFactory = filterOptionFactory;
  }

  @Override
  public Object get(DataFetchingEnvironment environment) {
    var graphDescription = this.createGraphDescription(environment);
    var type = environment.getFieldDefinition().getType();
    if (type instanceof GraphQLObjectType && (environment.containsArgument("id"))) {
      var id = (String) environment.getArgument("id");
      var output = this.graphLoader.get(
          new UniqueIdentifier(id),
          graphDescription,
          Object.class
      );
      return output.getData();

    }
    if (type instanceof GraphQLNonNull graphQLNonNull) {
      var innerType = graphQLNonNull.getWrappedType();
      if (innerType instanceof GraphQLList) {
        var output = this.graphLoader.find(
            graphDescription,
            Object.class
        );
        return output.getData();
      }
    }

    throw CannotLoadRequestedDataByGraphQL.becauseInvalidFieldInQuery(
        environment.getFieldDefinition()
    );
  }

  private NodeQueryGraphDescription createGraphDescription(DataFetchingEnvironment environment) {
    var type = environment.getFieldDefinition().getType();
    var unwrappedType = this.unwrapFromListOrNonnull(type);
    var documentDefinition =
        (OperationDefinition) environment.getDocument().getDefinitions().get(0);
    var mainSelectionSet = documentDefinition.getSelectionSet();
    var mainSelection = (Field) mainSelectionSet.getSelections().stream()
        .filter(Field.class::isInstance)
        .filter(selection -> ((Field) selection).getName()
            .equals(environment.getFieldDefinition().getName()))
        .toList().get(0);

    var innerSelectionSet = mainSelection.getSelectionSet();
    if (unwrappedType instanceof GraphQLObjectType graphQLObjectType) {
      var childGraphDescriptions = this.createChildGraphDescriptions(
          innerSelectionSet,
          graphQLObjectType
      );
      var arguments = mainSelection.getArguments();
      var searchQueryParameters = this.deserializeSearchQueryParameters(
          arguments,
          graphQLObjectType,
          childGraphDescriptions,
          GraphLoaderGqlDataFetcher.DEFAULT_MAIN_PAGINATION
      );
      return new NodeQueryGraphDescription(
          new NodeDescriptionParameters(graphQLObjectType.getName()),
          searchQueryParameters,
          childGraphDescriptions
      );
    }
    throw CannotLoadRequestedDataByGraphQL.becauseInvalidFieldInQuery(
        environment.getFieldDefinition());
  }

  private List<GraphDescription> createChildGraphDescriptions(
      SelectionSet selectionSet,
      GraphQLObjectType type
  ) {
    return selectionSet.getSelections().stream()
        .filter(Field.class::isInstance)
        .map(Field.class::cast)
        .filter(field -> !field.getName().equals("__typename"))
        .map(field -> this.createChildGraphDescription(field,
            type.getFieldDefinition(field.getName())))
        .toList();
  }

  private GraphDescription createChildGraphDescription(
      Field field,
      GraphQLFieldDefinition fieldDefinition
  ) {
    var unwrappedType = this.unwrapFromListOrNonnull(fieldDefinition.getType());
    if (unwrappedType instanceof GraphQLScalarType) {
      if (fieldDefinition.getName().equals("id")) {
        return new UuidIdentityDescription();
      }
      return new AttributeQueryDescription(field.getName());
    }
    if (unwrappedType instanceof GraphQLObjectType graphQLObjectType) {
      var childGraphDescriptions = this.createChildGraphDescriptions(
          field.getSelectionSet(),
          graphQLObjectType
      );
      var arguments = field.getArguments();
      var searchQueryParameters = this.deserializeSearchQueryParameters(
          arguments,
          graphQLObjectType,
          childGraphDescriptions,
          GraphLoaderGqlDataFetcher.DEFAULT_INNER_PAGINATION
      );
      var fixedSearchQueryParameters = this.fixChildSearchQueryParameters(
          graphQLObjectType.getName(),
          searchQueryParameters
      );
      var nodeQueryGraphDescription = new NodeQueryGraphDescription(
          new NodeDescriptionParameters(graphQLObjectType.getName()),
          SearchQueryParameters.from(),
          childGraphDescriptions
      );
      return new OutgoingEdgeQueryDescription(
          new EdgeDescriptionParameters(fieldDefinition.getName()),
          fixedSearchQueryParameters,
          nodeQueryGraphDescription
      );
    }
    throw CannotLoadRequestedDataByGraphQL.becauseInnerFieldWasNotObjectOrScalar(fieldDefinition);
  }

  private SearchQueryParameters deserializeSearchQueryParameters(
      List<Argument> arguments,
      GraphQLObjectType type,
      List<GraphDescription> childGraphDescriptions,
      OffsetPaginationOption defaultPagination
  ) {
    var optionalPagination = arguments.stream()
        .filter(argument -> argument.getName().equals("pagination"))
        .findFirst();
    var optionalSort = arguments.stream()
        .filter(argument -> argument.getName().equals("sort"))
        .findFirst();
    var optionalFilter = arguments.stream()
        .filter(argument -> argument.getName().equals("filter"))
        .findFirst();

    var parametersBuilder = SearchQueryParameters.builder();
    if (optionalPagination.isPresent()) {
      var pagination = optionalPagination.get();
      parametersBuilder.setPaginationOption(this.deserializeOffsetPaginationOption(pagination));
    } else {
      parametersBuilder.setPaginationOption(defaultPagination);
    }
    if (optionalSort.isPresent()) {
      var sort = optionalSort.get();
      parametersBuilder.addSortOptions(
          this.deserializeSortOptions(sort, type, childGraphDescriptions)
      );
    }
    if (optionalFilter.isPresent()) {
      var filter = optionalFilter.get();
      parametersBuilder.addFilterOptions(this.deserializeFilterOptions(filter, type));
    }
    return parametersBuilder.build();
  }

  @NotNull
  private OffsetPaginationOption deserializeOffsetPaginationOption(Argument pagination) {
    var paginationValue = (ObjectValue) pagination.getValue();
    var fields = paginationValue.getObjectFields();
    var limit = (IntValue) fields.stream()
        .filter(paginationField -> paginationField.getName().equals("limit"))
        .findFirst()
        .orElseThrow()
        .getValue();

    var offset = (IntValue) fields.stream()
        .filter(paginationField -> paginationField.getName().equals("offset"))
        .findFirst()
        .orElseThrow()
        .getValue();

    return new OffsetPaginationOption(offset.getValue().intValue(), limit.getValue().intValue());
  }

  private List<SortOption> deserializeSortOptions(
      Argument sort,
      GraphQLObjectType type,
      List<GraphDescription> childGraphDescriptions
  ) {
    var sortValue = (ArrayValue) sort.getValue();
    return sortValue.getValues().stream()
        .map(value -> this.deserializeSortOption((ObjectValue) value, type, childGraphDescriptions))
        .toList();
  }

  private List<FilterOption<?>> deserializeFilterOptions(
      Argument filter,
      GraphQLObjectType type
  ) {
    var filterValue = (ArrayValue) filter.getValue();
    var filterOptions = new ArrayList<FilterOption<?>>();
    filterValue.getValues().forEach(
        value -> filterOptions.add(
            this.deserializeFilterOption((ObjectValue) value, type)
        )
    );
    return filterOptions;
  }

  private SortOption deserializeSortOption(
      ObjectValue sortOptionValue,
      GraphQLObjectType type,
      List<GraphDescription> childGraphDescriptions
  ) {
    var fields = sortOptionValue.getObjectFields();
    if (fields.size() != 1) {
      throw CannotLoadRequestedDataByGraphQL.becauseProvidedSortOptionDidNotHaveExactlyOneField(sortOptionValue);
    }
    var field = fields.get(0);
    if (field.getValue() instanceof EnumValue enumValue) {
      if (enumValue.getName().equals("ASC")) {
        return new AscendingSortOption(field.getName());
      } else {
        return new DescendingSortOption(field.getName());
      }
    }
    if (field.getValue() instanceof ObjectValue objectValue) {
      var fieldName = field.getName();
      var fieldType = type.getFieldDefinition(fieldName).getType();
      var unwrappedType = (GraphQLObjectType) this.unwrapFromListOrNonnull(fieldType);
      var maybeInnerEdgeDescription = childGraphDescriptions.stream()
          .filter(OutgoingEdgeQueryDescription.class::isInstance)
          .map(OutgoingEdgeQueryDescription.class::cast)
          .filter(outgoingEdgeQueryDescription -> {
            var params = (EdgeDescriptionParameters) outgoingEdgeQueryDescription.getParameters();
            return params.getEdgeType().equals(fieldName);
          })
          .findFirst();

      List<GraphDescription> newChildGraphDescriptions = new ArrayList<>();
      var edgeSearchOptions = SearchQueryParameters.from();
      var nodeSearchOptions = SearchQueryParameters.from();
      if (maybeInnerEdgeDescription.isPresent()) {
        var innerEdgeDescription = maybeInnerEdgeDescription.get();
        edgeSearchOptions = innerEdgeDescription.getSearchQueryParameters();
        var maybeInnerNodeDescription = innerEdgeDescription.getChildGraphDescriptions().stream()
            .filter(NodeQueryGraphDescription.class::isInstance)
            .map(NodeQueryGraphDescription.class::cast)
            .findFirst();
        if (maybeInnerNodeDescription.isPresent()) {
          var innerNodeDescription = maybeInnerNodeDescription.get();
          nodeSearchOptions = innerNodeDescription.getSearchQueryParameters();
          newChildGraphDescriptions = innerNodeDescription.getChildGraphDescriptions();
        }
      }
      var innerSort = this.deserializeSortOption(objectValue, unwrappedType, newChildGraphDescriptions);
      var newGraphDescription = new OutgoingEdgeQueryDescription(
          new EdgeDescriptionParameters(fieldName),
          edgeSearchOptions,
          new NodeQueryGraphDescription(
              new NodeDescriptionParameters(unwrappedType.getName()),
              nodeSearchOptions,
              innerSort.getParameters()
          )
      );
      if (innerSort instanceof DescendingSortOption) {
        return new DescendingSortOption(newGraphDescription);
      } else {
        return new AscendingSortOption(newGraphDescription);
      }
    }
    throw CannotLoadRequestedDataByGraphQL.becauseProvidedSortOptionWasOfUnknownType(field.getValue());
  }

  private FilterOption<?> deserializeFilterOption(
      ObjectValue filterOptionValue,
      GraphQLObjectType type
  ) {
    var fields = filterOptionValue.getObjectFields();
    if (fields.size() != 1) {
      throw CannotLoadRequestedDataByGraphQL.becauseProvidedFilterOptionDidNotHaveExactlyOneField(
          filterOptionValue);
    }
    var field = fields.get(0);
    var fieldName = field.getName();
    var fieldValue = field.getValue();
    int underscoreIndex = fieldName.indexOf("_");
    if (underscoreIndex == -1) {
      if (StringUtils.isAllUpperCase(fieldName)) {
        var filterStrategy = fieldName.toLowerCase();
        if (GraphQlFilterInputGenerator.MULTI_LOGICAL_FILTER_STRATEGIES.contains(filterStrategy)) {
          if (fieldValue instanceof ArrayValue arrayValue) {
            var invalidValueItems = arrayValue.getValues().stream()
                .filter(itemValue -> !(itemValue instanceof ObjectValue))
                .toList();

            if (!invalidValueItems.isEmpty()) {
              throw CannotLoadRequestedDataByGraphQL.becauseProvidedLogicalFilterInputHadInvalidType(
                  filterStrategy,
                  invalidValueItems
              );
            }

            var children = new ArrayList<FilterOption<?>>();
            arrayValue.getValues().stream()
                .map(ObjectValue.class::cast)
                .map(itemValue -> this.deserializeFilterOption(itemValue, type))
                .forEach(children::add);

            return this.filterOptionFactory.createLogical(filterStrategy, children);
          }
          throw CannotLoadRequestedDataByGraphQL.becauseProvidedMultiLogicalFilterInputWasNotOfArrayValue(
              filterStrategy,
              fieldValue
          );
        }
        if (filterStrategy.equals(NotFilterOption.STRATEGY)) {
          if (fieldValue instanceof ObjectValue objectValue) {
            var child = this.deserializeFilterOption(objectValue, type);

            return this.filterOptionFactory.createLogical(filterStrategy, List.of(child));
          }
          throw CannotLoadRequestedDataByGraphQL.becauseProvidedLogicalFilterInputHadInvalidType(
              filterStrategy,
              List.of(fieldValue)
          );
        }
      }
      if (fieldValue instanceof ObjectValue objectValue) {
        var fieldType = type.getFieldDefinition(fieldName).getType();
        var unwrappedType = (GraphQLObjectType) this.unwrapFromListOrNonnull(fieldType);
        var innerFilter = this.deserializeFilterOption(objectValue, unwrappedType);
        var innerGraphDescription = this.filterOptionFactory.getAttributeNamePath(innerFilter);
        var newGraphDescription = new OutgoingEdgeQueryDescription(
            new EdgeDescriptionParameters(fieldName),
            SearchQueryParameters.from(),
            new NodeQueryGraphDescription(
                new NodeDescriptionParameters(unwrappedType.getName()),
                SearchQueryParameters.from(),
                innerGraphDescription
            )
        );
        return this.filterOptionFactory.copyWithNewAttributeNamePath(
            innerFilter,
            newGraphDescription
        );
      }
      throw CannotLoadRequestedDataByGraphQL.becauseProvidedDeepFilterWasNotOfObjectValue(
          fieldName,
          fieldValue
      );
    } else {
      var attributeName = fieldName.substring(0, underscoreIndex);
      var filterStrategy = fieldName.substring(underscoreIndex + 1).toLowerCase();
      if (GraphQlFilterInputGenerator.LEAF_FILTER_STRATEGIES.contains(filterStrategy)) {
        return this.filterOptionFactory.createLeaf(
            filterStrategy,
            attributeName,
            this.deserializeScalarValue(field.getValue())
        );
      }
      if (GraphQlFilterInputGenerator.LIST_FILTER_STRATEGIES.contains(filterStrategy)) {
        if (fieldValue instanceof ObjectValue objectValue) {
          var innerFields = objectValue.getObjectFields();
          if (innerFields.size() != 1) {
            throw CannotLoadRequestedDataByGraphQL.becauseProvidedFilterOptionDidNotHaveExactlyOneField(objectValue);
          }
          var innerField = innerFields.get(0);
          var innerFieldName = innerField.getName();
          if (StringUtils.isAllUpperCase(innerFieldName.replace("_", ""))) {
            var leafFilter = this.filterOptionFactory.createLeaf(
                innerFieldName.toLowerCase(),
                attributeName,
                this.deserializeScalarValue(innerField.getValue())
            );
            return this.filterOptionFactory.createList(
                filterStrategy,
                leafFilter
            );
          }
          var fieldType = type.getFieldDefinition(attributeName).getType();
          var unwrappedType = (GraphQLObjectType) this.unwrapFromListOrNonnull(fieldType);
          var innerFilter = this.deserializeFilterOption(objectValue, unwrappedType);
          var innerGraphDescription = this.filterOptionFactory.getAttributeNamePath(innerFilter);
          var newGraphDescription = new OutgoingEdgeQueryDescription(
              new EdgeDescriptionParameters(attributeName),
              SearchQueryParameters.from(),
              this.getCollectionComparisonOperatorFromFilterStrategy(filterStrategy),
              new NodeQueryGraphDescription(
                  new NodeDescriptionParameters(unwrappedType.getName()),
                  SearchQueryParameters.from(),
                  innerGraphDescription
              )
          );
          return this.filterOptionFactory.copyWithNewAttributeNamePath(
              innerFilter,
              newGraphDescription
          );
        }
        throw CannotLoadRequestedDataByGraphQL.becauseProvidedListFilterInputWasNotOfObjectValue(
            filterStrategy,
            attributeName,
            fieldValue
        );
      }
      throw CannotLoadRequestedDataByGraphQL.becauseProvidedFilterOptionWasOfUnknownStrategy(filterStrategy);
    }
  }

  private SearchQueryParameters fixChildSearchQueryParameters(
      String nodeType,
      SearchQueryParameters searchQueryParameters
  ) {
    var builder = SearchQueryParameters.builder();
    builder.setPaginationOption(searchQueryParameters.getPaginationOption());
    searchQueryParameters.getSortOptions().forEach(sort -> {
      var newGraphDescription = new NodeDescription(
          nodeType,
          sort.getParameters()
      );
      if (sort instanceof DescendingSortOption) {
        builder.addSortOption(new DescendingSortOption(newGraphDescription));
      } else {
        builder.addSortOption(new AscendingSortOption(newGraphDescription));
      }
    });
    
    searchQueryParameters.getFilterOptions().forEach(filter -> {
      var newGraphDescription = new NodeDescription(
          nodeType,
          this.filterOptionFactory.getAttributeNamePath(filter)
      );
      builder.addFilterOption(this.filterOptionFactory.copyWithNewAttributeNamePath(filter, newGraphDescription));
    });
    return builder.build();
  }

  private Object deserializeScalarValue(Value<?> value) {
    if (value instanceof StringValue stringValue) {
      return stringValue.getValue();
    }
    if (value instanceof BooleanValue booleanValue) {
      return booleanValue.isValue();
    }
    if (value instanceof FloatValue floatValue) {
      return floatValue.getValue().floatValue();
    }
    if (value instanceof IntValue intValue) {
      return intValue.getValue().intValue();
    }
    return value;
  }

  @NotNull
  private CollectionComparisonOperator getCollectionComparisonOperatorFromFilterStrategy(String filterStrategy) {
    if (filterStrategy.equals(NoneMatchFilterOption.STRATEGY)) {
      return CollectionComparisonOperator.NONE;
    }
    if (filterStrategy.equals(AllMatchFilterOption.STRATEGY)) {
      return CollectionComparisonOperator.ALL;
    }
    return CollectionComparisonOperator.ANY;
  }

  private GraphQLType unwrapFromListOrNonnull(GraphQLType graphQLType) {
    if (graphQLType instanceof GraphQLList graphQLList) {
      return this.unwrapFromListOrNonnull(graphQLList.getWrappedType());
    }
    if (graphQLType instanceof GraphQLNonNull graphQLNonNull) {
      return this.unwrapFromListOrNonnull(graphQLNonNull.getWrappedType());
    }
    return graphQLType;
  }

  private GraphQLType unwrapFromNonnull(GraphQLType graphQLType) {
    if (graphQLType instanceof GraphQLNonNull graphQLNonNull) {
      return this.unwrapFromNonnull(graphQLNonNull.getWrappedType());
    }
    return graphQLType;
  }
}
