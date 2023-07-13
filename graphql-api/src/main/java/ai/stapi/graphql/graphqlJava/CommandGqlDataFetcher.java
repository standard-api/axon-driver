package ai.stapi.graphql.graphqlJava;

import ai.stapi.graphql.graphqlJava.exceptions.CannotLoadRequestedDataByGraphQL;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionStructureTypeMapper;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import ai.stapi.schema.structureSchema.AbstractStructureType;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.FieldType;
import ai.stapi.schema.structureSchema.PrimitiveStructureType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaProvider;
import ai.stapi.schema.structureSchemaProvider.exception.CannotProvideStructureSchema;
import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.Field;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.ObjectValue;
import graphql.language.OperationDefinition;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.gateway.CommandGateway;

public class CommandGqlDataFetcher implements DataFetcher<CommandGqlDataFetcher.MutationResponse> {

  private final CommandGateway commandGateway;
  private final AggregateDefinitionProvider aggregateDefinitionProvider;
  private final StructureSchemaProvider graphDefinitionProvider;
  private final OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper;

  public CommandGqlDataFetcher(
      CommandGateway commandGateway,
      AggregateDefinitionProvider aggregateDefinitionProvider,
      StructureSchemaProvider graphDefinitionProvider,
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper
  ) {
    this.commandGateway = commandGateway;
    this.aggregateDefinitionProvider = aggregateDefinitionProvider;
    this.graphDefinitionProvider = graphDefinitionProvider;
    this.operationDefinitionStructureTypeMapper = operationDefinitionStructureTypeMapper;
  }

  @Override
  public MutationResponse get(DataFetchingEnvironment environment) throws Exception {
    var commandName = StringUtils.capitalize(environment.getFieldDefinition().getName());

    var operationDefinition = this.aggregateDefinitionProvider.provideAll().stream()
        .flatMap(aggregateDefinitionDTO -> aggregateDefinitionDTO.getCommand().stream())
        .map(CommandHandlerDefinitionDTO::getOperation)
        .filter(operation -> operation.getId().equals(commandName))
        .findFirst();

    if (operationDefinition.isEmpty()) {
      throw CannotLoadRequestedDataByGraphQL.becauseProvidedMutationDidNotHaveCorrespondingOperationDefinition(
          commandName
      );
    }

    var documentDefinition = (OperationDefinition) environment.getDocument()
        .getDefinitions()
        .get(0);

    var mainSelectionSet = documentDefinition.getSelectionSet();
    var mainSelection = mainSelectionSet.getSelections().stream()
        .filter(Field.class::isInstance)
        .map(Field.class::cast)
        .filter(selection -> selection.getName().equals(environment.getFieldDefinition().getName()))
        .toList()
        .get(0);

    var arguments = mainSelection.getArguments();
    var originalPayload = arguments.stream()
        .filter(argument -> argument.getName().equals("payload"))
        .findFirst()
        .orElseThrow()
        .getValue();

    Map<?, ?> mapPayload;
    if (originalPayload instanceof ObjectValue objectValue) {
      mapPayload = this.transformToMap(objectValue);
    } else {
      mapPayload = (Map<?, ?>) environment.getVariables().get("payload");
    }
    var fakedType = this.operationDefinitionStructureTypeMapper.map(
        operationDefinition.get()
    );

    var finalPayload = this.fixInnerPayload(
        fakedType.getDefinitionType(),
        fakedType,
        mapPayload
    );

    String id = environment.getArgument("id");
    UniqueIdentifier identifier;
    if (id == null) {
      identifier = UniversallyUniqueIdentifier.randomUUID();
    } else {
      identifier = UniqueIdentifier.fromString(id);
    }

    var command = new DynamicCommand(identifier, commandName, finalPayload);
    
    this.commandGateway.send(command);
    return new MutationResponse(true);
  }

  private Map<String, Object> fixInnerPayload(
      String commandName,
      ComplexStructureType parentStructureType,
      Map<?, ?> originalPayload
  ) {
    var finalPayload = new HashMap<String, Object>();
    parentStructureType.getAllFields().values().forEach(fieldDefinition -> {
      var fieldName = fieldDefinition.getName();
      if (originalPayload.keySet().stream().noneMatch(key -> key.toString().equals(fieldName))) {
        return;
      }
      var originalValue = originalPayload.entrySet().stream()
          .filter(field -> field.getKey().equals(fieldName))
          .findFirst()
          .orElseThrow()
          .getValue();

      if (fieldDefinition.isUnionType()) {
        if (originalValue instanceof List<?> listOriginalValue) {
          finalPayload.put(
              fieldName,
              listOriginalValue.stream().map(valueItem ->
                  this.getUnionMember(
                      commandName,
                      parentStructureType.getDefinitionType(),
                      fieldDefinition.getName(),
                      fieldDefinition.getTypes(),
                      valueItem
                  )
              ).collect(Collectors.toList())
          );
          return;
        } else {
          var unionMemberValue = this.getUnionMember(
              commandName,
              parentStructureType.getDefinitionType(),
              fieldDefinition.getName(),
              fieldDefinition.getTypes(),
              originalValue
          );
          finalPayload.put(fieldName, unionMemberValue);
          return;
        }
      }
      var fieldType = fieldDefinition.getTypes().get(0).getType();
      AbstractStructureType structure;
      try {
        structure = this.graphDefinitionProvider.provideSpecific(fieldType);
      } catch (CannotProvideStructureSchema e) {
        throw CannotLoadRequestedDataByGraphQL.becauseProvidedMutationHadParameterOfComplexTypeWithSomeFieldWhichDoesNotHaveStructureSchema(
            commandName,
            parentStructureType.getDefinitionType(),
            fieldName,
            fieldType,
            e
        );
      }

      if (structure instanceof PrimitiveStructureType) {
        if (originalValue instanceof List<?> listValue) {
          finalPayload.put(fieldName, listValue);
          return;
        } else {
          finalPayload.put(fieldName, originalValue);
          return;
        }
      }
      if (fieldDefinition.getTypes().get(0).isReference()) {
        if (originalValue instanceof List<?> listValue) {
          finalPayload.put(fieldName, listValue.stream().map(id -> Map.of("id", id)));
          return;
        } else {
          finalPayload.put(fieldName, Map.of("id", originalValue));
          return;
        }
      }
      if (structure instanceof ComplexStructureType complexStructureType) {
        if (originalValue instanceof Map<?, ?> objectOriginalParameter) {
          finalPayload.put(
              fieldName,
              this.fixInnerPayload(commandName, complexStructureType, objectOriginalParameter)
          );
          return;
        }
        if (originalValue instanceof List<?> arrayValue) {
          finalPayload.put(
              fieldName,
              arrayValue.stream().map(originalItem ->
                  this.fixInnerPayload(
                      commandName,
                      complexStructureType,
                      (Map<?, ?>) originalItem
                  )
              ).collect(Collectors.toList())
          );
          return;
        }
      }
      throw CannotLoadRequestedDataByGraphQL.becauseProvidedMutationHadParameterOfComplexTypeWithSomeFieldOfUnknownType(
          commandName,
          parentStructureType.getDefinitionType(),
          fieldName,
          fieldType
      );
    });
    return finalPayload;
  }

  private Object getUnionMember(
      String commandName,
      String parentTypeName,
      String fieldName,
      List<FieldType> fieldTypes,
      Object originalFieldValue
  ) {
    if (!(originalFieldValue instanceof Map<?, ?> objectValue)) {
      throw CannotLoadRequestedDataByGraphQL.becauseProvidedMutationHadParameterOfComplexTypeWithSomeInvalidUnionValue(
          commandName,
          parentTypeName,
          fieldName,
          originalFieldValue
      );
    }
    if (objectValue.keySet().size() != 1) {
      throw CannotLoadRequestedDataByGraphQL.becauseUnionInputDidNotHaveExactlyOneMemberSpecified(
          commandName,
          parentTypeName,
          fieldName,
          originalFieldValue
      );
    }

    var unionMemberField = objectValue.entrySet().stream()
        .findFirst()
        .orElseThrow();
    var memberType = (String) unionMemberField.getKey();
    var memberValue = unionMemberField.getValue();
    var fieldStructureType = fieldTypes.stream()
        .filter(fieldType -> fieldType.isBoxed() ? fieldType.getOriginalType().equals(memberType)
            : fieldType.getType().equals(memberType))
        .findFirst()
        .orElseThrow();

    AbstractStructureType structure;
    try {
      structure = this.graphDefinitionProvider.provideSpecific(memberType);
    } catch (CannotProvideStructureSchema e) {
      throw CannotLoadRequestedDataByGraphQL.becauseProvidedMutationHadParameterOfComplexTypeWithSomeFieldWhichDoesNotHaveStructureSchema(
          commandName,
          parentTypeName,
          fieldName,
          memberType,
          e
      );
    }
    if (structure instanceof PrimitiveStructureType) {
      return Map.of(
          DynamicCommand.SERIALIZATION_TYPE_FIELD_NAME,
          String.format("Boxed%s", StringUtils.capitalize(memberType)),
          "value", memberValue
      );
    }
    if (fieldStructureType.isReference()) {
      return Map.of(
          DynamicCommand.SERIALIZATION_TYPE_FIELD_NAME, memberType,
          "id", memberValue
      );
    }
    if (structure instanceof ComplexStructureType complexStructureType) {
      var resultingMap = new HashMap<String, Object>();
      resultingMap.put(
          DynamicCommand.SERIALIZATION_TYPE_FIELD_NAME,
          memberType
      );
      resultingMap.putAll(
          this.fixInnerPayload(
              commandName,
              complexStructureType,
              (Map<?, ?>) memberValue
          )
      );
      return resultingMap;
    }
    throw CannotLoadRequestedDataByGraphQL.becauseProvidedMutationHadParameterOfComplexTypeWithSomeFieldOfUnknownType(
        commandName,
        parentTypeName,
        fieldName,
        memberType
    );
  }

  private Map<String, Object> transformToMap(ObjectValue originalPayload) {
    var result = new HashMap<String, Object>();
    for (var field : originalPayload.getObjectFields()) {
      var fieldName = field.getName();
      var fieldValue = field.getValue();
      if (fieldValue instanceof ObjectValue objectValue) {
        // recursively transform nested object values
        result.put(fieldName, this.transformToMap(objectValue));
      } else if (fieldValue instanceof ArrayValue arrayValue) {
        // transform array values
        var arrayResult = new ArrayList<Object>();
        for (var itemValue : arrayValue.getValues()) {
          if (itemValue instanceof ObjectValue itemObjectValue) {
            arrayResult.add(this.transformToMap(itemObjectValue));
          } else {
            arrayResult.add(this.deserializeScalarValue(itemValue));
          }
        }
        result.put(fieldName, arrayResult);
      } else {
        result.put(fieldName, this.deserializeScalarValue(fieldValue));
      }
    }
    return result;
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

  public static class MutationResponse {


    private final boolean success;

    public MutationResponse(boolean success) {
      this.success = success;
    }

    public boolean getSuccess() {
      return success;
    }
  }
}
