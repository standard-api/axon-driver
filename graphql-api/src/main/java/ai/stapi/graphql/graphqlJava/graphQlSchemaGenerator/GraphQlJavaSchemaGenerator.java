package ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator;

import ai.stapi.graphql.generateGraphQlSchema.GraphQlSchemaGenerator;
import ai.stapi.graphql.graphqlJava.CommandGqlDataFetcher;
import ai.stapi.graphql.graphqlJava.GraphLoaderGqlDataFetcher;
import ai.stapi.graphql.graphqlJava.exceptions.CannotGenerateGraphQLSchema;
import ai.stapi.schema.structureSchema.BoxedPrimitiveStructureType;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.FieldDefinition;
import ai.stapi.schema.structureSchema.FieldType;
import ai.stapi.schema.structureSchema.PrimitiveStructureType;
import ai.stapi.schema.structureSchema.ResourceStructureType;
import ai.stapi.schema.structureSchema.StructureSchema;
import graphql.Scalars;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInputType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import graphql.schema.idl.SchemaPrinter;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class GraphQlJavaSchemaGenerator implements GraphQlSchemaGenerator {

  protected static final GraphQLInputObjectType OFFSET_PAGINATION = GraphQLInputObjectType.newInputObject()
          .name("OffsetPaginationOption")
          .field(
              GraphQLInputObjectField.newInputObjectField()
                  .name("offset")
                  .type(GraphQLNonNull.nonNull(Scalars.GraphQLInt))
          ).field(
              GraphQLInputObjectField.newInputObjectField()
                  .name("limit")
                  .type(GraphQLNonNull.nonNull(Scalars.GraphQLInt))
          ).build();
  protected static final GraphQLObjectType MUTATION_RESPONSE = GraphQLObjectType.newObject()
      .name("MutationResponse")
      .field(
          GraphQLFieldDefinition.newFieldDefinition()
              .name("successes")
              .type(Scalars.GraphQLBoolean)
              .build()
      ).build();
  protected static final String ANY_NODE = "AnyNode";
  private final GraphQlSortInputGenerator graphQlSortInputGenerator;
  private final GraphQlFilterInputGenerator graphQlFilterInputGenerator;
  private final GraphQlObjectTypeGenerator graphQlObjectTypeGenerator;
  private final GraphQlScalarSchemaGenerator graphQlScalarSchemaGenerator;
  private final GraphLoaderGqlDataFetcher graphLoaderGqlDataFetcher;
  private final CommandGqlDataFetcher commandGqlDataFetcher;

  public GraphQlJavaSchemaGenerator(
      GraphQlSortInputGenerator graphQlSortInputGenerator,
      GraphQlFilterInputGenerator graphQlFilterInputGenerator,
      GraphQlObjectTypeGenerator graphQlObjectTypeGenerator,
      GraphQlScalarSchemaGenerator graphQlScalarSchemaGenerator,
      GraphLoaderGqlDataFetcher graphLoaderGqlDataFetcher,
      CommandGqlDataFetcher commandGqlDataFetcher
  ) {
    this.graphQlSortInputGenerator = graphQlSortInputGenerator;
    this.graphQlFilterInputGenerator = graphQlFilterInputGenerator;
    this.graphQlObjectTypeGenerator = graphQlObjectTypeGenerator;
    this.graphQlScalarSchemaGenerator = graphQlScalarSchemaGenerator;
    this.graphLoaderGqlDataFetcher = graphLoaderGqlDataFetcher;
    this.commandGqlDataFetcher = commandGqlDataFetcher;
  }

  @Override
  public String generate(
      StructureSchema graphDefinition,
      List<ComplexStructureType> operationDefinitions
  ) {
    var qlSchema = this.generateSchema(graphDefinition, operationDefinitions);
    var printer = new SchemaPrinter(
        SchemaPrinter.Options.defaultOptions()
            .includeScalarTypes(true)
            .includeIntrospectionTypes(false)
    );

    return printer.print(qlSchema);
  }

  public GraphQLSchema generateSchema(
      StructureSchema graphDefinition,
      List<ComplexStructureType> operationDefinitions
  ) {
    var schema = GraphQLSchema.newSchema();
    var codeRegistry = GraphQLCodeRegistry.newCodeRegistry();

    this.generateQuery(graphDefinition, schema, codeRegistry);
    this.generateMutations(operationDefinitions, graphDefinition, schema, codeRegistry);

    return schema.codeRegistry(codeRegistry.build()).build();
  }

  private void generateQuery(
      StructureSchema graphDefinition,
      GraphQLSchema.Builder schema,
      GraphQLCodeRegistry.Builder codeRegistry
  ) {
    var sortInputs = this.graphQlSortInputGenerator.generateSortInputs(graphDefinition);
    sortInputs.values().forEach(schema::additionalType);
    var filterInputs = this.graphQlFilterInputGenerator.generateFilterInputs(graphDefinition);
    filterInputs.values().forEach(schema::additionalType);

    this.generateMainQueryObject(graphDefinition, schema, codeRegistry, sortInputs, filterInputs);
    this.generateComplexTypes(graphDefinition, schema, sortInputs, filterInputs);
    this.generatePrimitiveTypes(graphDefinition, schema);
    this.generateAnyNodeUnion(graphDefinition, schema);
  }

  private void generateMainQueryObject(
      StructureSchema graphDefinition,
      GraphQLSchema.Builder schema,
      GraphQLCodeRegistry.Builder codeRegistry,
      Map<String, GraphQLInputObjectType> sortInputs,
      Map<String, GraphQLInputObjectType> filterInputs
  ) {
    var queryObjectBuilder = GraphQLObjectType.newObject().name("Query");
    graphDefinition.getStructureTypes().values().stream()
        .filter(definition -> definition instanceof ResourceStructureType)
        .forEach(definition -> this.addResourceToQueryObjectAndCodeRegistry(
            queryObjectBuilder,
            codeRegistry,
            definition.getDefinitionType(),
            sortInputs,
            filterInputs
        ));

    schema.query(queryObjectBuilder);
  }

  private void generateComplexTypes(
      StructureSchema graphDefinition,
      GraphQLSchema.Builder schema,
      Map<String, GraphQLInputObjectType> sortInputs,
      Map<String, GraphQLInputObjectType> filterInputs
  ) {
    graphDefinition.getStructureTypes().values().stream()
        .filter(definition -> definition instanceof ComplexStructureType)
        .map(definition -> this.graphQlObjectTypeGenerator.generateObjectType(
            definition,
            graphDefinition,
            sortInputs,
            filterInputs
        )).flatMap(List::stream).forEach(schema::additionalType);
  }

  private void generatePrimitiveTypes(StructureSchema graphDefinition,
                                      GraphQLSchema.Builder schema) {
    graphDefinition.getStructureTypes().values().stream()
        .filter(definition -> definition instanceof PrimitiveStructureType)
        .map(definition -> (PrimitiveStructureType) definition)
        .map(this.graphQlScalarSchemaGenerator::generateScalarType)
        .forEach(schema::additionalType);
  }

  private void generateAnyNodeUnion(StructureSchema graphDefinition, GraphQLSchema.Builder schema) {
    var anyNodeUnion = new GraphQLUnionType.Builder()
        .name(ANY_NODE)
        .description("High level Union type of all types in this schema.")
        .typeResolver(env -> null);

    var concreteStructureDefinitions = graphDefinition.getStructureTypes().values()
        .stream()
        .filter(definition -> !(definition instanceof PrimitiveStructureType))
        .filter(definition -> !definition.isAbstract())
        .toList();

    if (!concreteStructureDefinitions.isEmpty()) {
      concreteStructureDefinitions.forEach(definition ->
          anyNodeUnion.possibleType(
              new GraphQLTypeReference(definition.getDefinitionType())
          )
      );
      schema.additionalType(anyNodeUnion.build());
    }
  }

  private void addResourceToQueryObjectAndCodeRegistry(
      GraphQLObjectType.Builder builder,
      GraphQLCodeRegistry.Builder codeRegistry,
      String typeName,
      Map<String, GraphQLInputObjectType> sortInputTypes,
      Map<String, GraphQLInputObjectType> filterInputTypes
  ) {
    var getFieldName = "get" + typeName;
    var findFieldName = "find" + typeName + "List";
    builder.field(
        GraphQLFieldDefinition.newFieldDefinition()
            .name(getFieldName)
            .type(new GraphQLTypeReference(typeName))
            .argument(
                GraphQLArgument.newArgument()
                    .name("id")
                    .type(GraphQLNonNull.nonNull(Scalars.GraphQLString))
                    .build()
            )
    );
    var filterFieldDefinitionBuilder = GraphQLFieldDefinition.newFieldDefinition()
        .name(findFieldName)
        .type(GraphQLNonNull.nonNull(
                GraphQLList.list(
                    GraphQLNonNull.nonNull(
                        new GraphQLTypeReference(typeName)
                    )
                )
            )
        );
    var sortInputType = sortInputTypes.get(
        this.graphQlSortInputGenerator.createSortInputTypeName(typeName)
    );
    var filterInputType = filterInputTypes.get(
        this.graphQlFilterInputGenerator.createLogicalFilterInputTypeName(typeName)
    );
    if (sortInputType != null) {
      filterFieldDefinitionBuilder.argument(
          GraphQLArgument.newArgument()
              .name("sort")
              .type(GraphQLList.list(GraphQLNonNull.nonNull(sortInputType)))
      );
    }
    if (filterInputType != null) {
      filterFieldDefinitionBuilder.argument(
          GraphQLArgument.newArgument()
              .name("filter")
              .type(GraphQLList.list(GraphQLNonNull.nonNull(filterInputType)))
      );
    }
    filterFieldDefinitionBuilder.argument(
        GraphQLArgument.newArgument()
            .name("pagination")
            .type(GraphQlJavaSchemaGenerator.OFFSET_PAGINATION)
    );
    builder.field(filterFieldDefinitionBuilder);
    codeRegistry.dataFetcher(
        FieldCoordinates.coordinates("Query", getFieldName),
        this.graphLoaderGqlDataFetcher
    );
    codeRegistry.dataFetcher(
        FieldCoordinates.coordinates("Query", findFieldName),
        this.graphLoaderGqlDataFetcher
    );
  }

  private void generateMutations(
      List<ComplexStructureType> operationDefinitions,
      StructureSchema graphDefinition,
      GraphQLSchema.Builder schema,
      GraphQLCodeRegistry.Builder codeRegistry
  ) {
    if (operationDefinitions.isEmpty()) {
      return;
    }
    var mutationObjectBuilder = GraphQLObjectType.newObject().name("Mutation");
    operationDefinitions.stream()
        .filter(
            type -> !type.getAllFields().values().stream().allMatch(FieldDefinition::isUnionType))
        .forEach(
            structureType -> this.generateCommand(
                graphDefinition,
                structureType,
                mutationObjectBuilder,
                codeRegistry
            )
        );

    schema.mutation(mutationObjectBuilder);
    this.generateComplexInputTypes(graphDefinition, schema);
  }

  private void generateCommand(
      StructureSchema graphDefinition,
      ComplexStructureType structureType,
      GraphQLObjectType.Builder mutationBuilder,
      GraphQLCodeRegistry.Builder codeRegistry
  ) {
    var commandPayloadName = this.createCommandPayloadName(structureType.getDefinitionType());
    var commandPayloadInputBuilder = GraphQLInputObjectType.newInputObject()
        .name(commandPayloadName)
        .description(structureType.getDescription());

    structureType.getAllFields().values().stream()
        .filter(field -> !field.getName().equals("id"))
        .forEach(field -> {
          if (field.getTypes().isEmpty()) {
            throw CannotGenerateGraphQLSchema.becauseOperationReferencedTypeWhichDoesNotExistInStructureSchema(
                structureType.getDefinitionType(),
                "Any"
            );
          }
          if (field.getTypes().size() > 1) {
            var fakeUnionTypeBuilder = GraphQLInputObjectType.newInputObject()
                .name(commandPayloadName + StringUtils.capitalize(field.getName()))
                .description(
                    "Generated input object which works as a union type, "
                        + "only exactly one field should be specified."
                );
            field.getTypes().forEach(fieldType -> {
              var type = fieldType.getType();
              var fieldStructureType = graphDefinition.getDefinition(type);
              if (fieldStructureType == null) {
                throw CannotGenerateGraphQLSchema.becauseOperationReferencedTypeWhichDoesNotExistInStructureSchema(
                    structureType.getDefinitionType(),
                    type
                );
              }

              GraphQLType graphQLInputType;

              if (fieldStructureType instanceof PrimitiveStructureType) {
                graphQLInputType = GraphQlObjectTypeGenerator.FIELD_TYPE_SPECIAL_CASES.getOrDefault(
                    type,
                    new GraphQLTypeReference(type)
                );
              } else if (fieldType.isReference()) {
                graphQLInputType = Scalars.GraphQLID;
              } else if (fieldStructureType instanceof BoxedPrimitiveStructureType) {
                var unboxed = this.unbox(type);
                graphQLInputType = GraphQlObjectTypeGenerator.FIELD_TYPE_SPECIAL_CASES.getOrDefault(
                    unboxed,
                    new GraphQLTypeReference(unboxed)
                );
              } else {
                graphQLInputType = new GraphQLTypeReference(this.createInputTypeName(type));
              }
              fakeUnionTypeBuilder.field(
                  GraphQLInputObjectField.newInputObjectField()
                      .name(
                          fieldStructureType instanceof BoxedPrimitiveStructureType ?
                              this.unbox(type) :
                              type
                      ).type((GraphQLInputType) graphQLInputType)
              );
            });
            GraphQLInputType fakeUnionType = fakeUnionTypeBuilder.build();
            if (field.getMax().equals("*") || Integer.parseInt(field.getMax()) > 1) {
              fakeUnionType = GraphQLList.list(GraphQLNonNull.nonNull(fakeUnionType));
            }
            if (field.getMin() > 0) {
              fakeUnionType = GraphQLNonNull.nonNull(fakeUnionType);
            }
            commandPayloadInputBuilder.field(
                GraphQLInputObjectField
                    .newInputObjectField()
                    .name(field.getName())
                    .type(fakeUnionType)
                    .build()
            );
            return;
          }

          var type = field.getTypes().get(0).getType();
          var fieldStructureType = graphDefinition.getDefinition(type);
          if (fieldStructureType == null) {
            throw CannotGenerateGraphQLSchema.becauseOperationReferencedTypeWhichDoesNotExistInStructureSchema(
                structureType.getDefinitionType(),
                type
            );
          }
          GraphQLType graphQLInputType;
          if (fieldStructureType instanceof PrimitiveStructureType) {
            graphQLInputType = GraphQlObjectTypeGenerator.FIELD_TYPE_SPECIAL_CASES.getOrDefault(
                type,
                new GraphQLTypeReference(type)
            );
          } else if (fieldStructureType instanceof ResourceStructureType) {
            graphQLInputType = Scalars.GraphQLID;
          } else {
            graphQLInputType = new GraphQLTypeReference(this.createInputTypeName(type));
          }
          if (field.getMax().equals("*") || Integer.parseInt(field.getMax()) > 1) {
            graphQLInputType = GraphQLList.list(GraphQLNonNull.nonNull(graphQLInputType));
          }
          if (field.getMin() > 0) {
            graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
          }
          commandPayloadInputBuilder.field(
              GraphQLInputObjectField
                  .newInputObjectField()
                  .name(field.getName())
                  .type((GraphQLInputType) graphQLInputType)
                  .build()
          );
        });

    var mutationFieldName = StringUtils.uncapitalize(structureType.getDefinitionType());
    mutationBuilder.field(
        GraphQLFieldDefinition
            .newFieldDefinition()
            .name(mutationFieldName)
            .argument(GraphQLArgument.newArgument().name("id").type(Scalars.GraphQLID))
            .argument(this.createPayloadArgument(commandPayloadInputBuilder))
            .type(MUTATION_RESPONSE)
            .build()
    );
    codeRegistry.dataFetcher(
        FieldCoordinates.coordinates("Mutation", mutationFieldName),
        this.commandGqlDataFetcher
    );

  }

  private void generateComplexInputTypes(
      StructureSchema graphDefinition,
      GraphQLSchema.Builder schema
  ) {
    //The only structure definition of kind Resource we need as input, is a resource itself
    if (graphDefinition.containsDefinition("Resource")) {
      var resourceType = graphDefinition.getDefinition("Resource");
      var inputType = this.createInputType((ComplexStructureType) resourceType);
      schema.additionalType(inputType);
    }
    graphDefinition.getStructureTypes().values().stream()
        .filter(ComplexStructureType.class::isInstance)
        .filter(abstractStructureType -> !abstractStructureType.getKind()
            .equals(ResourceStructureType.KIND))
        .map(ComplexStructureType.class::cast)
        .map(this::createInputType)
        .forEach(schema::additionalType);
  }

  private GraphQLInputObjectType createInputType(ComplexStructureType complexStructureType) {
    var inputTypeName = this.createInputTypeName(complexStructureType.getDefinitionType());
    var inputTypeBuilder = GraphQLInputObjectType.newInputObject().name(inputTypeName);

    complexStructureType.getAllFields().values().forEach(
        field -> createInputTypeField(inputTypeBuilder, field, inputTypeName)
    );

    return inputTypeBuilder.build();
  }

  private void createInputTypeField(
      GraphQLInputObjectType.Builder inputTypeBuilder,
      FieldDefinition field,
      String parentTypeName
  ) {
    if (field.isAnyType()) {
      return;
//      throw CannotGenerateGraphQLSchema.becauseStructureSchemaFieldHadNoType(
//          parentTypeName,
//          field.getName()
//      );
    }

    if (!field.isUnionType()) {
      var type = field.getTypes().get(0);
      var graphQLInputType = this.createInputFieldType(type);
      inputTypeBuilder.field(
          new GraphQLInputObjectField.Builder()
              .name(field.getName())
              .type(this.makeInputTypeRequiredAndListIfNeeded(field, graphQLInputType))
      );
      return;
    }
    var fakeUnionType = GraphQLInputObjectType.newInputObject()
        .name(parentTypeName + StringUtils.capitalize(field.getName()))
        .description(
            "Generated Input Type, which works as a Union type. Only one field should be filled.");

    field.getTypes().forEach(
        type -> {
          var graphQLInputType = this.createInputFieldType(type);
          fakeUnionType.field(
              new GraphQLInputObjectField.Builder()
                  .name(type.isBoxed() ? type.getOriginalType() : type.getType())
                  .type(graphQLInputType)
          );
        }
    );
    inputTypeBuilder.field(
        new GraphQLInputObjectField.Builder()
            .name(field.getName())
            .type(this.makeInputTypeRequiredAndListIfNeeded(field, fakeUnionType.build()))
    );
  }

  private GraphQLInputType createInputFieldType(FieldType type) {
    if (type.isReference()) {
      return Scalars.GraphQLID;
    } else {
      var typeString = type.isBoxed() ? type.getOriginalType() : type.getType();
      if (type.isPrimitiveType() || type.isBoxed()) {
        return (GraphQLInputType) GraphQlObjectTypeGenerator.FIELD_TYPE_SPECIAL_CASES.getOrDefault(
            typeString,
            new GraphQLTypeReference(typeString)
        );
      }
      return new GraphQLTypeReference(this.createInputTypeName(typeString));
    }
  }

  private GraphQLInputType makeInputTypeRequiredAndListIfNeeded(
      FieldDefinition field,
      GraphQLInputType graphQLInputType
  ) {
    if (field.isList()) {
      graphQLInputType = GraphQLList.list(GraphQLNonNull.nonNull(graphQLInputType));
    }
    if (field.isRequired()) {
      graphQLInputType = GraphQLNonNull.nonNull(graphQLInputType);
    }
    return graphQLInputType;
  }

  private GraphQLArgument.Builder createPayloadArgument(
      GraphQLInputObjectType.Builder inputBuilder) {
    return GraphQLArgument
        .newArgument()
        .name("payload")
        .type(GraphQLNonNull.nonNull(inputBuilder.build()));
  }

  @NotNull
  private String createCommandPayloadName(String operationName) {
    return operationName + "CommandPayloadInput";
  }

  private String createInputTypeName(String type) {
    return type + "CommandInput";
  }

  @NotNull
  private String unbox(String type) {
    return StringUtils.uncapitalize(type.replace("Boxed", ""));
  }
}
