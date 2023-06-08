package ai.stapi.axonsystem.dynamic.aggregate.exceptions;

import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;

public class CannotProvideDynamicAggregateConfigurationFromAggregateDefinition
    extends RuntimeException {

  private CannotProvideDynamicAggregateConfigurationFromAggregateDefinition(String becauseMessage) {
    super("Cannot provide DynamicAggregateConfiguration from OperationDefinition, because "
        + becauseMessage);
  }

  public static CannotProvideDynamicAggregateConfigurationFromAggregateDefinition becauseOperationHasNoneOrMoreResources(
      OperationDefinitionDTO operationDefinitionDTO
  ) {
    return new CannotProvideDynamicAggregateConfigurationFromAggregateDefinition(
        "operation definition has none or more resources." +
            "\nOperation definition: " + operationDefinitionDTO
    );
  }

  public static CannotProvideDynamicAggregateConfigurationFromAggregateDefinition becauseCommandHandlerDefinitionHasUnknownCreationalPolicy(
      CommandHandlerDefinitionDTO commandDefinition
  ) {
    return new CannotProvideDynamicAggregateConfigurationFromAggregateDefinition(
        "command handler definition has unknown creational policy." +
            "\nCommand handler definition: " + commandDefinition
    );
  }
}
