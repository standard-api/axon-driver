package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.graphsystem.messaging.event.AggregateGraphUpdatedEvent;
import ai.stapi.axonsystem.graphaggregate.AggregateWithDynamicGraph;
import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.identity.UniqueIdentifier;

public class DynamicAggregate extends AggregateWithDynamicGraph<UniqueIdentifier> {

  private String aggregateType;

  protected DynamicAggregate() {
  }

  public DynamicAggregate(
      DynamicCommandProcessor commandProcessor,
      String aggregateType
  ) {
    super(commandProcessor);
    this.aggregateType = aggregateType;
  }

  public DynamicAggregate(
      DynamicCommandProcessor commandProcessor,
      String aggregateType,
      DynamicCommand command
  ) {
    super(commandProcessor);
    this.aggregateType = aggregateType;
    this.processCommandDynamically(command);
  }

  public void handle(DynamicCommand command) {
    this.processCommandDynamically(command);
  }

  public void onEvent(AggregateGraphUpdatedEvent<UniqueIdentifier> event) {
    this.onAggregateCreated(event);
  }

  public String getAggregateType() {
    return aggregateType;
  }
}
