package ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator;

import ai.stapi.schema.structureSchema.AbstractStructureType;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.FieldDefinition;
import ai.stapi.schema.structureSchema.FieldType;
import ai.stapi.schema.structureSchema.StructureSchema;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLInputObjectField;
import graphql.schema.GraphQLInputObjectType;
import graphql.schema.GraphQLTypeReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class GraphQlSortInputGenerator {

  protected static final GraphQLEnumType SORT_DIRECTION_ENUM = GraphQLEnumType.newEnum()
      .name("SortDirection")
      .value("ASC")
      .value("DESC")
      .build();

  public Map<String, GraphQLInputObjectType> generateSortInputs(StructureSchema structureSchema) {
    var sortInputTypeMap = new HashMap<String, GraphQLInputObjectType>();
    structureSchema.getStructureTypes().values().stream()
        .filter(ComplexStructureType.class::isInstance)
        .map(ComplexStructureType.class::cast)
        .flatMap(definition -> this.generate(
            definition,
            structureSchema
        ).stream())
        .forEach(sortInput -> sortInputTypeMap.put(sortInput.getName(), sortInput));

    return sortInputTypeMap;
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
    var builder = new GraphQLInputObjectType.Builder().name(this.createSortInputTypeName(name));

    fieldsAndSubTypes.fields().forEach(builder::field);
    if (!fieldsAndSubTypes.fields().isEmpty()) {
      types.add(builder.build());
    }

    types.addAll(fieldsAndSubTypes.subtypes());
    return types;
  }

  private GraphQlSortInputGenerator.SortFieldsAndSubtypes createFieldsAndSubTypes(
      ComplexStructureType structureType,
      StructureSchema structureSchema
  ) {
    var fields = new ArrayList<GraphQLInputObjectField>();
    var subtypes = new ArrayList<GraphQLInputObjectType>();
    structureType.getAllFields().values().forEach(
        fieldDefinition -> {
          var fieldAndSubtypes = this.createFieldAndSubtypes(
              fieldDefinition,
              structureSchema
          );
          if (fieldAndSubtypes != null) {
            fields.add(fieldAndSubtypes.field());
            subtypes.addAll(fieldAndSubtypes.subtypes());
          }
        }
    );

    return new GraphQlSortInputGenerator.SortFieldsAndSubtypes(fields, subtypes);
  }

  @Nullable
  private GraphQlSortInputGenerator.SortFieldAndSubtypes createFieldAndSubtypes(
      FieldDefinition fieldDefinition,
      StructureSchema structureSchema
  ) {
    var types = fieldDefinition.getTypes().stream()
        .map(FieldType::getType)
        .toList();
    var subtypes = new ArrayList<GraphQLInputObjectType>();
    if (types.isEmpty()) {
      return new GraphQlSortInputGenerator.SortFieldAndSubtypes(
          GraphQLInputObjectField.newInputObjectField()
              .name(fieldDefinition.getName())
              .type(SORT_DIRECTION_ENUM).build(),
          subtypes
      );
    }

    var size = types.size();
    if (size > GraphQlObjectTypeGenerator.THE_ANSWER_TO_THE_ULTIMATE_QUESTION_OF_LIFE_THE_UNIVERSE_AND_EVERYTHING) {
      return new GraphQlSortInputGenerator.SortFieldAndSubtypes(
          GraphQLInputObjectField.newInputObjectField()
              .name(fieldDefinition.getName())
              .type(SORT_DIRECTION_ENUM).build(),
          subtypes
      );
    }
    var typeGraphDefinitions = types.stream()
        .map(type -> {
          var definition = structureSchema.getDefinition(type);
          if (definition == null) {
            throw new RuntimeException(
                "Definition for type '" + type + "' does not exist in schema."
            );
          }
          return definition;
        })
        .toList();

    if (typeGraphDefinitions.stream().allMatch(
        definition -> definition.getKind().equals(AbstractStructureType.PRIMITIVE_TYPE))) {
      return new GraphQlSortInputGenerator.SortFieldAndSubtypes(
          GraphQLInputObjectField.newInputObjectField()
              .name(fieldDefinition.getName())
              .type(SORT_DIRECTION_ENUM).build(),
          subtypes
      );
    }
    if (size > 1) {
      return null;
    }

    var typeName = types.get(0);
    return new GraphQlSortInputGenerator.SortFieldAndSubtypes(
        GraphQLInputObjectField.newInputObjectField()
            .name(fieldDefinition.getName())
            .type(new GraphQLTypeReference(this.createSortInputTypeName(typeName)))
            .build(),
        subtypes
    );
  }

  public String createSortInputTypeName(String originalTypeName) {
    return originalTypeName + "SortOption";
  }

  protected record SortFieldsAndSubtypes(
      List<GraphQLInputObjectField> fields,
      List<GraphQLInputObjectType> subtypes
  ) {

  }

  protected record SortFieldAndSubtypes(
      GraphQLInputObjectField field,
      List<GraphQLInputObjectType> subtypes
  ) {

  }
}
