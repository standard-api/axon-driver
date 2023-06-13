package ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator;

import ai.stapi.graph.attribute.attributeValue.Base64BinaryAttributeValue;
import ai.stapi.graph.attribute.attributeValue.BooleanAttributeValue;
import ai.stapi.graph.attribute.attributeValue.CanonicalAttributeValue;
import ai.stapi.graph.attribute.attributeValue.CodeAttributeValue;
import ai.stapi.graph.attribute.attributeValue.DateAttributeValue;
import ai.stapi.graph.attribute.attributeValue.DateTimeAttributeValue;
import ai.stapi.graph.attribute.attributeValue.DecimalAttributeValue;
import ai.stapi.graph.attribute.attributeValue.IdAttributeValue;
import ai.stapi.graph.attribute.attributeValue.InstantAttributeValue;
import ai.stapi.graph.attribute.attributeValue.IntegerAttributeValue;
import ai.stapi.graph.attribute.attributeValue.MarkdownAttributeValue;
import ai.stapi.graph.attribute.attributeValue.OidAttributeValue;
import ai.stapi.graph.attribute.attributeValue.PositiveIntegerAttributeValue;
import ai.stapi.graph.attribute.attributeValue.StringAttributeValue;
import ai.stapi.graph.attribute.attributeValue.TimeAttributeValue;
import ai.stapi.graph.attribute.attributeValue.UnsignedIntegerAttributeValue;
import ai.stapi.graph.attribute.attributeValue.UriAttributeValue;
import ai.stapi.graph.attribute.attributeValue.UrlAttributeValue;
import ai.stapi.graph.attribute.attributeValue.UuidAttributeValue;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AllMatchFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AndFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.AnyMatchFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.ContainsFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.EndsWithFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.EqualsFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.GreaterThanFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.GreaterThanOrEqualFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.LowerThanFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.LowerThanOrEqualsFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.NoneMatchFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.NotEqualsFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.NotFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.OrFilterOption;
import ai.stapi.graphoperations.graphLoader.search.filterOption.StartsWithFilterOption;
import ai.stapi.schema.structureSchema.AbstractStructureType;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.FieldDefinition;
import ai.stapi.schema.structureSchema.FieldType;
import ai.stapi.schema.structureSchema.PrimitiveStructureType;
import ai.stapi.schema.structureSchema.StructureSchema;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class GraphQlFilterInputGenerator {

  public static final List<String> LEAF_FILTER_STRATEGIES = List.of(
      ContainsFilterOption.STRATEGY,
      EndsWithFilterOption.STRATEGY,
      EqualsFilterOption.STRATEGY,
      GreaterThanFilterOption.STRATEGY,
      GreaterThanOrEqualFilterOption.STRATEGY,
      LowerThanFilterOption.STRATEGY,
      LowerThanOrEqualsFilterOption.STRATEGY,
      NotEqualsFilterOption.STRATEGY,
      StartsWithFilterOption.STRATEGY
  );

  public static final List<String> LIST_FILTER_STRATEGIES = List.of(
      AllMatchFilterOption.STRATEGY,
      AnyMatchFilterOption.STRATEGY,
      NoneMatchFilterOption.STRATEGY
  );
  public static final List<String> MULTI_LOGICAL_FILTER_STRATEGIES = List.of(
      AndFilterOption.STRATEGY,
      OrFilterOption.STRATEGY
  );
  private static final List<String> STRING_TYPES = List.of(
      Base64BinaryAttributeValue.SERIALIZATION_TYPE,
      CanonicalAttributeValue.SERIALIZATION_TYPE,
      CodeAttributeValue.SERIALIZATION_TYPE,
      IdAttributeValue.SERIALIZATION_TYPE,
      MarkdownAttributeValue.SERIALIZATION_TYPE,
      OidAttributeValue.SERIALIZATION_TYPE,
      StringAttributeValue.SERIALIZATION_TYPE,
      UriAttributeValue.SERIALIZATION_TYPE,
      UrlAttributeValue.SERIALIZATION_TYPE,
      UuidAttributeValue.SERIALIZATION_TYPE
  );

  private static final List<String> NUMBER_TYPES = List.of(
      DateAttributeValue.SERIALIZATION_TYPE,
      DateTimeAttributeValue.SERIALIZATION_TYPE,
      DecimalAttributeValue.SERIALIZATION_TYPE,
      InstantAttributeValue.SERIALIZATION_TYPE,
      IntegerAttributeValue.SERIALIZATION_TYPE,
      PositiveIntegerAttributeValue.SERIALIZATION_TYPE,
      TimeAttributeValue.SERIALIZATION_TYPE,
      UnsignedIntegerAttributeValue.SERIALIZATION_TYPE
  );
  private static final List<String> STRING_FILTER_STRATEGIES = List.of(
      ContainsFilterOption.STRATEGY,
      EndsWithFilterOption.STRATEGY,
      EqualsFilterOption.STRATEGY,
      GreaterThanFilterOption.STRATEGY,
      GreaterThanOrEqualFilterOption.STRATEGY,
      LowerThanFilterOption.STRATEGY,
      LowerThanOrEqualsFilterOption.STRATEGY,
      NotEqualsFilterOption.STRATEGY,
      StartsWithFilterOption.STRATEGY
  );

  private static final List<String> NUMBER_FILTER_STRATEGIES = List.of(
      EqualsFilterOption.STRATEGY,
      GreaterThanFilterOption.STRATEGY,
      GreaterThanOrEqualFilterOption.STRATEGY,
      LowerThanFilterOption.STRATEGY,
      LowerThanOrEqualsFilterOption.STRATEGY,
      NotEqualsFilterOption.STRATEGY
  );

  private static final List<String> DEFAULT_FILTER_STRATEGIES = List.of(
      EqualsFilterOption.STRATEGY,
      NotEqualsFilterOption.STRATEGY
  );

  public Map<String, GraphQLInputObjectType> generateFilterInputs(StructureSchema structureSchema) {
    var filterInputTypeMap = new HashMap<String, GraphQLInputObjectType>();
    structureSchema.getStructureTypes().values().stream()
        .filter(definition -> definition instanceof ComplexStructureType)
        .map(definition -> (ComplexStructureType) definition)
        .flatMap(definition -> this.generate(
            definition,
            structureSchema
        ).stream())
        .forEach(filterInput -> {
          filterInputTypeMap.put(filterInput.getName(), filterInput);
        });

    return filterInputTypeMap;
  }

  private List<GraphQLInputObjectType> generate(
      ComplexStructureType structureType,
      StructureSchema structureSchema
  ) {
    var fieldsAndSubTypes = this.createFieldsAndSubTypes(
        structureType,
        structureSchema
    );

    var name = structureType.getDefinitionType();
    var types = new ArrayList<GraphQLInputObjectType>();
    var logicalFilterInputTypeName = this.createLogicalFilterInputTypeName(name);
    var logicalBuilder = new GraphQLInputObjectType.Builder().name(logicalFilterInputTypeName);
    var filterInputTypeName = this.createFilterInputTypeName(name);
    var builder = new GraphQLInputObjectType.Builder().name(filterInputTypeName);

    fieldsAndSubTypes.fields().forEach(logicalBuilder::field);
    fieldsAndSubTypes.fields().forEach(builder::field);

    if (!fieldsAndSubTypes.fields().isEmpty()) {
      GraphQlFilterInputGenerator.MULTI_LOGICAL_FILTER_STRATEGIES.stream()
          .map(strategy -> new GraphQLInputObjectField.Builder()
              .name(strategy.toUpperCase())
              .type(GraphQLList.list(
                  GraphQLNonNull.nonNull(new GraphQLTypeReference(logicalFilterInputTypeName))))
          ).forEach(logicalBuilder::field);

      logicalBuilder.field(
          new GraphQLInputObjectField.Builder()
              .name(NotFilterOption.STRATEGY.toUpperCase())
              .type(new GraphQLTypeReference(logicalFilterInputTypeName))
      );

      types.add(logicalBuilder.build());
      types.add(builder.build());
    }

    types.addAll(fieldsAndSubTypes.subtypes());
    return types;
  }

  private GraphQlFilterInputGenerator.FilterFieldsAndSubtypes createFieldsAndSubTypes(
      ComplexStructureType structureType,
      StructureSchema structureSchema
  ) {
    var fields = new ArrayList<GraphQLInputObjectField>();
    var subtypes = new ArrayList<GraphQLInputObjectType>();
    structureType.getAllFields().values().forEach(
        fieldDefinition -> {
          var fieldAndSubtypes = this.createFieldsAndSubtypes(
              structureType,
              fieldDefinition,
              structureSchema
          );
          if (fieldAndSubtypes != null) {
            fields.addAll(fieldAndSubtypes.fields());
            subtypes.addAll(fieldAndSubtypes.subtypes());
          }
        }
    );

    return new GraphQlFilterInputGenerator.FilterFieldsAndSubtypes(fields, subtypes);
  }

  @Nullable
  private GraphQlFilterInputGenerator.FilterFieldsAndSubtypes createFieldsAndSubtypes(
      ComplexStructureType structureType,
      FieldDefinition fieldDefinition,
      StructureSchema structureSchema
  ) {
    var types = fieldDefinition.getTypes().stream()
        .map(FieldType::getType)
        .toList();
    var subtypes = new ArrayList<GraphQLInputObjectType>();
    if (types.isEmpty()) {
      return null;
    }

    var typeGraphDefinitions = types.stream()
        .map(structureSchema::getDefinition)
        .toList();

    if (types.size() > 1) {
      return null;
    }
    var typeGraphDefinition = typeGraphDefinitions.get(0);
    if (typeGraphDefinition instanceof PrimitiveStructureType) {
      if (this.isFieldList(fieldDefinition)) {
        var defaultFilterStrategiesFields =
            GraphQlFilterInputGenerator.DEFAULT_FILTER_STRATEGIES.stream()
                .map(strategy -> GraphQLInputObjectField.newInputObjectField()
                    .name(fieldDefinition.getName() + "_" + strategy.toUpperCase())
                    .type(GraphQLList.list(
                        GraphQLNonNull.nonNull(this.getSpecialOrDefaultType(types))))
                    .build()
                ).toList();

        var innerFields = this.getFilterGraphQLInputObjectFields(
            fieldDefinition,
            types,
            typeGraphDefinition,
            true
        );

        var childFilterInputTypeName = this.createFilterInputTypeName(
            structureType.getDefinitionType() +
                StringUtils.capitalize(fieldDefinition.getName()) +
                "Child"
        );
        var childOptionsBuilder = new GraphQLInputObjectType.Builder()
            .name(childFilterInputTypeName);

        innerFields.forEach(childOptionsBuilder::field);
        subtypes.add(childOptionsBuilder.build());

        var listFilterStrategiesFields = GraphQlFilterInputGenerator.LIST_FILTER_STRATEGIES.stream()
            .map(strategy -> GraphQLInputObjectField.newInputObjectField()
                .name(fieldDefinition.getName() + "_" + strategy.toUpperCase())
                .type(new GraphQLTypeReference(childFilterInputTypeName))
                .build()
            ).toList();

        var finalFields = new ArrayList<>(defaultFilterStrategiesFields);
        finalFields.addAll(listFilterStrategiesFields);

        return new GraphQlFilterInputGenerator.FilterFieldsAndSubtypes(
            finalFields,
            subtypes
        );
      }

      var fields = this.getFilterGraphQLInputObjectFields(
          fieldDefinition,
          types,
          typeGraphDefinition,
          false
      );

      return new GraphQlFilterInputGenerator.FilterFieldsAndSubtypes(
          fields,
          subtypes
      );
    }

    if (this.isFieldList(fieldDefinition)) {
      return new GraphQlFilterInputGenerator.FilterFieldsAndSubtypes(
          GraphQlFilterInputGenerator.LIST_FILTER_STRATEGIES.stream()
              .map(strategy -> GraphQLInputObjectField.newInputObjectField()
                  .name(fieldDefinition.getName() + "_" + strategy.toUpperCase())
                  .type(new GraphQLTypeReference(this.createFilterInputTypeName(types.get(0))))
                  .build()
              ).toList(),
          subtypes
      );
    }

    return new GraphQlFilterInputGenerator.FilterFieldsAndSubtypes(
        List.of(
            GraphQLInputObjectField.newInputObjectField()
                .name(fieldDefinition.getName())
                .type(new GraphQLTypeReference(this.createFilterInputTypeName(types.get(0))))
                .build()
        ),
        subtypes
    );
  }

  private List<GraphQLInputObjectField> getFilterGraphQLInputObjectFields(
      FieldDefinition fieldDefinition,
      List<String> types,
      AbstractStructureType typeGraphDefinition,
      boolean withShortNames
  ) {
    if (GraphQlFilterInputGenerator.STRING_TYPES.contains(
        typeGraphDefinition.getDefinitionType())) {
      return GraphQlFilterInputGenerator.STRING_FILTER_STRATEGIES.stream()
          .map(strategy -> GraphQLInputObjectField.newInputObjectField()
              .name(this.getFieldName(fieldDefinition, strategy, withShortNames))
              .type((GraphQLInputType) this.getSpecialOrDefaultType(types))
              .build()
          ).toList();
    }

    if (GraphQlFilterInputGenerator.NUMBER_TYPES.contains(
        typeGraphDefinition.getDefinitionType())) {
      return GraphQlFilterInputGenerator.NUMBER_FILTER_STRATEGIES.stream()
          .map(strategy -> GraphQLInputObjectField.newInputObjectField()
              .name(this.getFieldName(fieldDefinition, strategy, withShortNames))
              .type((GraphQLInputType) this.getSpecialOrDefaultType(types))
              .build()
          ).toList();
    }

    if (typeGraphDefinition.getDefinitionType().equals(BooleanAttributeValue.SERIALIZATION_TYPE)) {
      return GraphQlFilterInputGenerator.DEFAULT_FILTER_STRATEGIES.stream()
          .map(strategy -> GraphQLInputObjectField.newInputObjectField()
              .name(this.getFieldName(fieldDefinition, strategy, withShortNames))
              .type((GraphQLInputType) this.getSpecialOrDefaultType(types))
              .build()
          ).toList();
    }
    return new ArrayList<>();
  }

  @NotNull
  private String getFieldName(
      FieldDefinition fieldDefinition,
      String strategy,
      boolean withShortNames
  ) {
    if (withShortNames) {
      return strategy.toUpperCase();
    }
    return fieldDefinition.getName() + "_" + strategy.toUpperCase();
  }

  @NotNull
  private GraphQLType getSpecialOrDefaultType(List<String> types) {
    var name = types.get(0);
    return GraphQlObjectTypeGenerator.FIELD_TYPE_SPECIAL_CASES.getOrDefault(
        name,
        new GraphQLTypeReference(name)
    );
  }

  public String createLogicalFilterInputTypeName(String originalTypeName) {
    return originalTypeName + "LogicalFilterOption";
  }

  public String createFilterInputTypeName(String originalTypeName) {
    return originalTypeName + "FilterOption";
  }

  private boolean isFieldList(FieldDefinition fieldDefinition) {
    return fieldDefinition.getMax().equals("*") ||
        Integer.parseInt(fieldDefinition.getMax()) > 1;
  }

  protected record FilterFieldsAndSubtypes(
      List<GraphQLInputObjectField> fields,
      List<GraphQLInputObjectType> subtypes
  ) {

  }
}
