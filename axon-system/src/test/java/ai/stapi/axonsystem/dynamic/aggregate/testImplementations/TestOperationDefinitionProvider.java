package ai.stapi.axonsystem.dynamic.aggregate.testImplementations;

import ai.stapi.graphsystem.operationdefinition.exceptions.CannotProvideOperationDefinition;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO.ParameterDTO;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO.ParameterDTO.ReferencedFrom;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import java.util.List;

public class TestOperationDefinitionProvider implements OperationDefinitionProvider {

  private static final List<OperationDefinitionDTO> OPERATIONS = List.of(
      new OperationDefinitionDTO(
          TestAggregateDefinitionProvider.CREATE_VALUE_SET_COMMAND,
          "Create A New Value Set",
          "draft",
          "operation",
          "Command for creating a new Value Set",
          "CreateValueSet",
          List.of(TestAggregateDefinitionProvider.TARGET_RESOURCE),
          false,
          true,
          true,
          List.of(
              new ParameterDTO(
                  "targetAggregateIdentifier",
                  "in",
                  1,
                  "1",
                  "string",
                  new ReferencedFrom(TestAggregateDefinitionProvider.TARGET_RESOURCE),
                  List.of()
              ),
              new ParameterDTO(
                  "name",
                  "in",
                  1,
                  "1",
                  "string",
                  new ReferencedFrom(TestAggregateDefinitionProvider.TARGET_RESOURCE),
                  List.of()
              )
          )
      ),
      new OperationDefinitionDTO(
          TestAggregateDefinitionProvider.CHANGE_VALUE_SET_COMMAND,
          "Changes A Existing Value Set",
          "draft",
          "operation",
          "Command for changing a Value Set",
          "ChangeValueSet",
          List.of(TestAggregateDefinitionProvider.TARGET_RESOURCE),
          false,
          true,
          true,
          List.of(
              new ParameterDTO(
                  "targetAggregateIdentifier",
                  "in",
                  1,
                  "1",
                  "string",
                  new ReferencedFrom(TestAggregateDefinitionProvider.TARGET_RESOURCE),
                  List.of()
              ),
              new ParameterDTO(
                  "name",
                  "in",
                  1,
                  "1",
                  "string",
                  new ReferencedFrom(TestAggregateDefinitionProvider.TARGET_RESOURCE),
                  List.of()
              )
          )
      ),
      new OperationDefinitionDTO(
          TestAggregateDefinitionProvider.CREATE_IF_MISSING_VALUE_SET_COMMAND,
          "Changes A Existing Value Set Or Creates it",
          "draft",
          "operation",
          "Command for changing or creating a Value Set",
          "CreateIfMissingValueSet",
          List.of(TestAggregateDefinitionProvider.TARGET_RESOURCE),
          false,
          true,
          true,
          List.of(
              new ParameterDTO(
                  "targetAggregateIdentifier",
                  "in",
                  1,
                  "1",
                  "string",
                  new ReferencedFrom(TestAggregateDefinitionProvider.TARGET_RESOURCE),
                  List.of()
              ),
              new ParameterDTO(
                  "name",
                  "in",
                  1,
                  "1"
                      + "",
                  "string",
                  new ReferencedFrom(TestAggregateDefinitionProvider.TARGET_RESOURCE),
                  List.of()
              )
          )
      )
  );

  public static OperationDefinitionDTO provideStatic(String operationId) {
    return OPERATIONS.stream()
        .filter(operation -> operation.getId().equals(operationId))
        .findAny()
        .orElseThrow(() -> new RuntimeException(new CannotProvideOperationDefinition(operationId)));
  }

  @Override
  public List<OperationDefinitionDTO> provideAll() {
    return OPERATIONS;
  }

  @Override
  public OperationDefinitionDTO provide(
      String operationId
  ) throws CannotProvideOperationDefinition {
    return TestOperationDefinitionProvider.provideStatic(operationId);
  }

  @Override
  public boolean contains(String operationId) {
    return OPERATIONS.stream().anyMatch(
        operation -> operation.getId().equals(operationId)
    );
  }
}
