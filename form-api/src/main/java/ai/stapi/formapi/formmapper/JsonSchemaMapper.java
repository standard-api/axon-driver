package ai.stapi.formapi.formmapper;

import ai.stapi.formapi.formmapper.exceptions.CannotMapJsonSchema;
import ai.stapi.formapi.formmapper.exceptions.CannotPrintJSONSchema;
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
import ai.stapi.graph.attribute.attributeValue.XhtmlAttributeValue;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionStructureTypeMapper;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.FieldDefinition;
import ai.stapi.schema.structureSchema.FieldType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.everit.json.schema.ArraySchema;
import org.everit.json.schema.BooleanSchema;
import org.everit.json.schema.CombinedSchema;
import org.everit.json.schema.NumberSchema;
import org.everit.json.schema.ObjectSchema;
import org.everit.json.schema.ReferenceSchema;
import org.everit.json.schema.Schema;
import org.everit.json.schema.StringSchema;

public class JsonSchemaMapper {

  public static Set<String> STRING_LIKE_PRIMITIVES = Set.of(
      Base64BinaryAttributeValue.SERIALIZATION_TYPE,
      CanonicalAttributeValue.SERIALIZATION_TYPE,
      CodeAttributeValue.SERIALIZATION_TYPE,
      DateAttributeValue.SERIALIZATION_TYPE,
      DateTimeAttributeValue.SERIALIZATION_TYPE,
      TimeAttributeValue.SERIALIZATION_TYPE,
      IdAttributeValue.SERIALIZATION_TYPE,
      InstantAttributeValue.SERIALIZATION_TYPE,
      MarkdownAttributeValue.SERIALIZATION_TYPE,
      OidAttributeValue.SERIALIZATION_TYPE,
      UriAttributeValue.SERIALIZATION_TYPE,
      UrlAttributeValue.SERIALIZATION_TYPE,
      UuidAttributeValue.SERIALIZATION_TYPE,
      XhtmlAttributeValue.SERIALIZATION_TYPE,
      StringAttributeValue.SERIALIZATION_TYPE
  );

  public static Set<String> NUMBER_LIKE_PRIMITIVES = Set.of(
      DecimalAttributeValue.SERIALIZATION_TYPE,
      PositiveIntegerAttributeValue.SERIALIZATION_TYPE,
      UnsignedIntegerAttributeValue.SERIALIZATION_TYPE,
      IntegerAttributeValue.SERIALIZATION_TYPE
  );

  private final OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper;
  private final StructureSchemaFinder structureSchemaFinder;

  public JsonSchemaMapper(
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper,
      StructureSchemaFinder structureSchemaFinder
  ) {
    this.operationDefinitionStructureTypeMapper = operationDefinitionStructureTypeMapper;
    this.structureSchemaFinder = structureSchemaFinder;
  }

  public Map<String, Object> map(OperationDefinitionDTO operationDefinitionDTO) {
    return this.map(operationDefinitionDTO, true);
  }

  public Map<String, Object> map(
      OperationDefinitionDTO operationDefinitionDTO,
      Boolean omitExtension
  ) {
    var fakedStructureType = this.operationDefinitionStructureTypeMapper.map(
        operationDefinitionDTO
    );
    var formMapperContext = new FormMapperContext();
    var schema = this.getObjectSchema(fakedStructureType, formMapperContext, omitExtension);
    return this.printSchema(schema, formMapperContext);
  }

  private ObjectSchema getObjectSchema(
      ComplexStructureType structureType,
      FormMapperContext formMapperContext,
      Boolean omitExtension
  ) {
    return this.getObjectSchemaWithTitle(
        structureType,
        formMapperContext,
        omitExtension,
        structureType.getDefinitionType()
    );
  }

  private ObjectSchema getObjectSchemaWithTitle(
      ComplexStructureType structureType,
      FormMapperContext formMapperContext,
      Boolean omitExtension,
      String title
  ) {
    var builder = new ObjectSchema.Builder();
    builder.title(title);
    builder.description(
        structureType.getDescription()
    );
    structureType.getAllFields()
        .values()
        .stream()
        .filter(field -> !omitExtension || (!field.getName().equals("extension") &&
            !field.getName().equals("modifierExtension")))
        .sorted(Comparator.comparing(FieldDefinition::getName))
        .forEach(field -> this.mapField(field, builder, formMapperContext, omitExtension));

    return builder.build();
  }

  private void mapField(
      FieldDefinition field,
      ObjectSchema.Builder builder,
      FormMapperContext formMapperContext,
      Boolean omitExtension
  ) {
    var parameterName = field.getName();
    if (field.getMin() > 0) {
      builder.addRequiredProperty(parameterName);
    }
    if (field.getTypes().isEmpty()) {
      return;
    }
    if (field.getFloatMax() > 1) {
      this.mapArrayField(field, builder, formMapperContext, omitExtension);
      return;
    }
    var schema = this.getSchema(field, formMapperContext, omitExtension);
    builder.addPropertySchema(parameterName, schema);
  }

  private Schema getSchema(
      FieldDefinition field,
      FormMapperContext formMapperContext,
      Boolean omitExtension
  ) {
    if (field.getTypes().size() > 1) {
      var schemaBuilder = new CombinedSchema.Builder().criterion(CombinedSchema.ONE_CRITERION);
      field.getTypes().stream()
          .map(type -> this.getMemberSchema(type, field, formMapperContext, omitExtension))
          .forEach(schemaBuilder::subschema);

      return schemaBuilder.build();
    }
    var type = field.getTypes().get(0);
    return this.getMemberSchema(type, field, formMapperContext, omitExtension);
  }

  private Schema getMemberSchema(
      FieldType type,
      FieldDefinition fieldDefinition,
      FormMapperContext formMapperContext,
      Boolean omitExtension
  ) {
    var typeName = type.getType();
    if (type.isPrimitiveType()) {
      return this.getPrimitiveSchema(typeName, fieldDefinition);
    } else {
      var title = this.formatMachineReadableToHumanReadable(fieldDefinition.getName());
      if (type.isReference()) {
        return new StringSchema.Builder().title(title).description(
            fieldDefinition.getDescription()).build();
      } else {
        if (!formMapperContext.hasType(typeName)) {
          formMapperContext.addType(typeName);
          var structureType = (ComplexStructureType) this.structureSchemaFinder.getStructureType(
              typeName
          );
          var objectSchema = this.getObjectSchemaWithTitle(
              structureType,
              formMapperContext,
              omitExtension,
              title
          );
          formMapperContext.putSchema(typeName, objectSchema);
        }
        return new ReferenceSchema.Builder()
            .refValue(String.format("#/definitions/%s", typeName))
            .title(title)
            .description(fieldDefinition.getDescription())
            .build();
      }
    }
  }

  private Schema getPrimitiveSchema(String type, FieldDefinition fieldDefinition) {
    var title = this.formatMachineReadableToHumanReadable(fieldDefinition.getName());
    if (STRING_LIKE_PRIMITIVES.contains(type)) {
      return new StringSchema.Builder()
          .title(title)
          .description(fieldDefinition.getDescription())
          .build();
    }
    if (NUMBER_LIKE_PRIMITIVES.contains(type)) {
      return new NumberSchema.Builder()
          .title(title)
          .description(fieldDefinition.getDescription())
          .build();
    }
    if (type.equals(BooleanAttributeValue.SERIALIZATION_TYPE)) {
      return new BooleanSchema.Builder()
          .title(title)
          .description(fieldDefinition.getDescription())
          .build();
    }
    throw CannotMapJsonSchema.becauseUnknownPrimitiveTypeEncountered(type);
  }

  private void mapArrayField(
      FieldDefinition field,
      ObjectSchema.Builder builder,
      FormMapperContext formMapperContext,
      Boolean omitExtension
  ) {
    var itemSchema = this.getSchema(field, formMapperContext, omitExtension);
    String title = String.format("List of %s", formatMachineReadableToHumanReadable(field.getName()));
    builder.addPropertySchema(
        field.getName(),
        new ArraySchema.Builder()
            .allItemSchema(itemSchema)
            .title(title).build()
    );
  }

  private Map<String, Object> printSchema(ObjectSchema schema,
                                          FormMapperContext formMapperContext) {
    try {
      var map = new ObjectMapper().readValue(
          schema.toString(), new TypeReference<HashMap<String, Object>>() {
          });
      var definitionsMap = new HashMap<String, Object>();
      formMapperContext.getComplexTypeSchemas().forEach(
          (typeName, definition) -> definitionsMap.put(typeName, this.printDefinition(definition))
      );
      map.put("definitions", definitionsMap);
      return map;
    } catch (JsonProcessingException e) {
      throw new CannotPrintJSONSchema(e);
    }
  }

  private Map<String, Object> printDefinition(ObjectSchema schema) {
    try {
      return new ObjectMapper().readValue(
          schema.toString(), new TypeReference<HashMap<String, Object>>() {
          });
    } catch (JsonProcessingException e) {
      throw new CannotPrintJSONSchema(e);
    }
  }

  private static String formatMachineReadableToHumanReadable(String string) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < string.length(); i++) {
      if (shouldInsertSpace(string, i)) {
        result.append(" ");
      }
      result.append(i == 0 ? Character.toUpperCase(string.charAt(i)) : string.charAt(i));
    }
    return result.toString();
  }

  private static boolean shouldInsertSpace(String string, int index) {
    return isUppercase(string, index) 
        && (isStartOfWord(string, index) || isEndOfAcronym(string, index));
  }

  private static boolean isUppercase(String string, int index) {
    return Character.isUpperCase(string.charAt(index));
  }

  private static boolean isStartOfWord(String string, int index) {
    return index > 0 && !isUppercase(string, index - 1);
  }

  private static boolean isEndOfAcronym(String string, int index) {
    return index < string.length() - 1 && !isUppercase(string, index + 1);
  }

  private static class FormMapperContext {

    private final Set<String> encounteredComplexTypes;
    private final Map<String, ObjectSchema> complexTypeSchemas;

    public FormMapperContext() {
      this.encounteredComplexTypes = new HashSet<>();
      this.complexTypeSchemas = new HashMap<>();
    }

    public void putSchema(String typeName, ObjectSchema schema) {
      this.complexTypeSchemas.put(typeName, schema);
    }

    public void addType(String typeName) {
      this.encounteredComplexTypes.add(typeName);
    }

    public boolean hasType(String typeName) {
      return this.encounteredComplexTypes.contains(typeName);
    }

    public Set<String> getEncounteredComplexTypes() {
      return encounteredComplexTypes;
    }

    public Map<String, ObjectSchema> getComplexTypeSchemas() {
      return complexTypeSchemas;
    }
  }
}
