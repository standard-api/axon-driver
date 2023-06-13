package ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator;

import ai.stapi.schema.structureSchema.AbstractStructureType;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.FieldDefinition;
import ai.stapi.schema.structureSchema.FieldType;
import ai.stapi.schema.structureSchema.PrimitiveStructureType;
import ai.stapi.schema.structureSchema.StructureSchema;
import graphql.Scalars;
import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLInterfaceType;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeReference;
import graphql.schema.GraphQLUnionType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class GraphQlObjectTypeGenerator {

  /*
  FHIR has concept of any type. As defined at http://hl7.org/fhir/datatypes.html#open :
  'Some elements do not have a specified type. The type is represented by the wildcard symbol "*".'
  In Structure Definition this is defined as list of all 50 possible types. (as of current date 30.Nov.2022)
  Therefore there is arbitrary count of types above which we interpret the type as '*'
   */
  public static final Integer THE_ANSWER_TO_THE_ULTIMATE_QUESTION_OF_LIFE_THE_UNIVERSE_AND_EVERYTHING = 42;
  protected static final Map<String, GraphQLType> FIELD_TYPE_SPECIAL_CASES = Map.of(
      "string", Scalars.GraphQLString,
      "boolean", Scalars.GraphQLBoolean,
      "id", Scalars.GraphQLID,
      "integer", Scalars.GraphQLInt,
      "decimal", Scalars.GraphQLFloat
  );
  private final GraphQlSortInputGenerator graphQLSortInputGenerator;
  private final GraphQlFilterInputGenerator graphQlFilterInputGenerator;

  public GraphQlObjectTypeGenerator(
      GraphQlSortInputGenerator graphQLSortInputGenerator,
      GraphQlFilterInputGenerator graphQlFilterInputGenerator
  ) {
    this.graphQLSortInputGenerator = graphQLSortInputGenerator;
    this.graphQlFilterInputGenerator = graphQlFilterInputGenerator;
  }

  protected List<GraphQLType> generateObjectType(
      AbstractStructureType structureType,
      StructureSchema structureSchema,
      Map<String, GraphQLInputObjectType> sortInputTypes,
      Map<String, GraphQLInputObjectType> filterInputTypes
  ) {
    var fieldsInterfacesAndSubTypes = this.createFieldsInterfacesAndSubTypes(
        structureType,
        structureSchema,
        sortInputTypes,
        filterInputTypes
    );

    var name = structureType.getDefinitionType();
    if (structureType.isAbstract()) {
      var builder = new GraphQLInterfaceType.Builder()
          .name(name)
          .description(
              structureType.getDescription()
                  .replace("\r", "")
                  .replace("\n+", "\n")
          ).typeResolver(env -> null);

      fieldsInterfacesAndSubTypes.fields().forEach(builder::field);
      fieldsInterfacesAndSubTypes.interfaces()
          .forEach(interfaceType -> builder.withInterface((GraphQLTypeReference) interfaceType));
      var finalTypes = new ArrayList<GraphQLType>();
      finalTypes.add(builder.build());
      finalTypes.addAll(fieldsInterfacesAndSubTypes.subtypes);

      return finalTypes;
    } else {
      var builder = new GraphQLObjectType.Builder()
          .name(name)
          .description(structureType.getDescription());

      fieldsInterfacesAndSubTypes.fields().forEach(builder::field);
      fieldsInterfacesAndSubTypes.interfaces()
          .forEach(interfaceType -> builder.withInterface((GraphQLTypeReference) interfaceType));
      var finalTypes = new ArrayList<GraphQLType>();
      finalTypes.add(builder.build());
      finalTypes.addAll(fieldsInterfacesAndSubTypes.subtypes);

      return finalTypes;
    }
  }

  protected GraphQlObjectTypeGenerator.FieldsInterfacesSubtypes createFieldsInterfacesAndSubTypes(
      AbstractStructureType structureType,
      StructureSchema structureSchema,
      Map<String, GraphQLInputObjectType> sortInputTypes,
      Map<String, GraphQLInputObjectType> filterInputTypes
  ) {
    var graphQlFields = new ArrayList<GraphQLFieldDefinition.Builder>();
    var interfaces = this.createInterfaces(
        structureType,
        structureSchema
    );
    var subtypes = new ArrayList<GraphQLType>();
    Map<String, FieldDefinition> fields = new HashMap<>();
    if (structureType instanceof ComplexStructureType complexStructureType) {
      fields = complexStructureType.getAllFields();
    }
    fields.values().forEach(
        definition -> {
          var fieldAndSubtypes = this.createFieldAndSubtypes(
              definition,
              structureType,
              structureSchema,
              sortInputTypes,
              filterInputTypes
          );
          graphQlFields.add(fieldAndSubtypes.field());
          subtypes.addAll(fieldAndSubtypes.subtypes());
        }
    );

    return new GraphQlObjectTypeGenerator.FieldsInterfacesSubtypes(graphQlFields, interfaces,
        subtypes);
  }

  private List<GraphQLType> createInterfaces(
      AbstractStructureType structureType,
      StructureSchema structureSchema
  ) {
    var interfaces = new ArrayList<GraphQLType>();
    if (structureType instanceof ComplexStructureType complexStructureType
        && !complexStructureType.getParent().isBlank()) {
      var parentStructureType = structureSchema.getDefinition(complexStructureType.getParent());
      if (parentStructureType.isAbstract()) {
        interfaces.add(new GraphQLTypeReference(parentStructureType.getDefinitionType()));
      }
      var restOfInterfaces = this.createInterfaces(parentStructureType, structureSchema);
      interfaces.addAll(restOfInterfaces);
    }
    return interfaces;
  }

  private GraphQlObjectTypeGenerator.FieldAndSubtypes createFieldAndSubtypes(
      FieldDefinition fieldDefinition,
      AbstractStructureType structureType,
      StructureSchema structureSchema,
      Map<String, GraphQLInputObjectType> sortInputTypes,
      Map<String, GraphQLInputObjectType> filterInputTypes
  ) {
    var types = fieldDefinition.getTypes().stream()
        .map(FieldType::getType)
        .toList();
    GraphQLType type;
    String typeName;
    var subtypes = new ArrayList<GraphQLType>();
    //TODO: Change to this when we solve content reference in StructureDefinition to StructureSchema mapping
//        if (types.size() == 0) {
//            throw CannotGenerateGraphQLSchema.becauseStructureSchemaFieldHadNoType(
//                structureType.getDefinitionType(),
//                FieldDefinition.getName()
//            );
//        }
    if (types.isEmpty()
        || types.size() > THE_ANSWER_TO_THE_ULTIMATE_QUESTION_OF_LIFE_THE_UNIVERSE_AND_EVERYTHING) {
      type = new GraphQLTypeReference(GraphQlJavaSchemaGenerator.ANY_NODE);
      typeName = GraphQlJavaSchemaGenerator.ANY_NODE;
    } else if (types.size() == 1) {
      typeName = types.get(0);
      type = GraphQlObjectTypeGenerator.FIELD_TYPE_SPECIAL_CASES.getOrDefault(
          typeName,
          new GraphQLTypeReference(typeName)
      );
    } else {
      typeName =
          structureType.getDefinitionType() + StringUtils.capitalize(fieldDefinition.getName());
      type = new GraphQLTypeReference(typeName);

      var unionTypeBuilder = new GraphQLUnionType.Builder()
          .name(typeName)
          .description(
              "Type for union type field contained in " + structureType.getDefinitionType())
          .typeResolver(env -> null);

      var allObjectTypes = this.getAllConcreteTypes(types, structureSchema);
      allObjectTypes.forEach(
          typeInUnion -> unionTypeBuilder.possibleType(new GraphQLTypeReference(typeInUnion)));
      subtypes.add(unionTypeBuilder.build());
    }

    var typeStructureDefinition = structureSchema.getDefinition(typeName);
    if (type instanceof GraphQLScalarType
        || typeStructureDefinition instanceof PrimitiveStructureType) {
      return new GraphQlObjectTypeGenerator.FieldAndSubtypes(
          this.createField(
              fieldDefinition,
              fieldDefinition.getName(),
              type,
              typeName,
              sortInputTypes,
              filterInputTypes,
              true
          ),
          subtypes
      );
    }
    return new GraphQlObjectTypeGenerator.FieldAndSubtypes(
        this.createField(
            fieldDefinition,
            fieldDefinition.getName(),
            type,
            typeName,
            sortInputTypes,
            filterInputTypes,
            false
        ),
        subtypes
    );
  }

  private List<String> getAllConcreteTypes(List<String> types, StructureSchema structureSchema) {
    return types.stream()
        .map(type -> this.getConcreteTypes(type, structureSchema))
        .flatMap(List::stream)
        .collect(Collectors.toSet())
        .stream().toList();
  }

  private List<String> getConcreteTypes(String type, StructureSchema structureSchema) {
    var structureType = structureSchema.getDefinition(type);
    if (!structureType.isAbstract()) {
      return List.of(type);
    } else {
      var children = structureSchema.getChildDefinitions(type);
      var childTypes = children.stream().map(AbstractStructureType::getDefinitionType).toList();
      return this.getAllConcreteTypes(childTypes, structureSchema);
    }
  }

  private GraphQLFieldDefinition.Builder createField(
      FieldDefinition fieldDefinition,
      String fieldName,
      GraphQLType graphQLType,
      String typeName,
      Map<String, GraphQLInputObjectType> sortInputTypes,
      Map<String, GraphQLInputObjectType> filterInputTypes,
      boolean isScalar
  ) {
    if (fieldDefinition.getMax().equals("*") || Integer.parseInt(fieldDefinition.getMax()) > 1) {
      graphQLType = GraphQLNonNull.nonNull(
          GraphQLList.list(
              GraphQLNonNull.nonNull(graphQLType)
          )
      );
    } else if (fieldDefinition.getMin() == 1) {
      graphQLType = GraphQLNonNull.nonNull(graphQLType);
    }

    var elementBuilder = GraphQLFieldDefinition.newFieldDefinition()
        .name(fieldName)
        .type((GraphQLOutputType) graphQLType);

    if (!fieldDefinition.getMax().equals("1")) {
      var sortInputType =
          sortInputTypes.get(this.graphQLSortInputGenerator.createSortInputTypeName(typeName));
      var filterInputType = filterInputTypes.get(
          this.graphQlFilterInputGenerator.createLogicalFilterInputTypeName(typeName));
      if (sortInputType != null) {
        elementBuilder.argument(
            GraphQLArgument.newArgument()
                .name("sort")
                .type(GraphQLList.list(GraphQLNonNull.nonNull(sortInputType)))
        );
      }
      if (filterInputType != null) {
        elementBuilder.argument(
            GraphQLArgument.newArgument()
                .name("filter")
                .type(GraphQLList.list(GraphQLNonNull.nonNull(filterInputType)))
        );
      }
      if (!isScalar) {
        elementBuilder.argument(
            GraphQLArgument.newArgument()
                .name("pagination")
                .type(GraphQlJavaSchemaGenerator.OFFSET_PAGINATION)
        );
      }
    }

    if (fieldDefinition.getDescription() != null) {
      elementBuilder.description(
          fieldDefinition.getDescription()
              .replace("\r", "")
              .replace("\n+", "\n")
      );
    }
    return elementBuilder;
  }

  protected record FieldsInterfacesSubtypes(
      List<GraphQLFieldDefinition.Builder> fields,
      List<GraphQLType> interfaces,
      List<GraphQLType> subtypes
  ) {

  }

  private record FieldAndSubtypes(
      GraphQLFieldDefinition.Builder field,
      List<GraphQLType> subtypes
  ) {

  }
}
