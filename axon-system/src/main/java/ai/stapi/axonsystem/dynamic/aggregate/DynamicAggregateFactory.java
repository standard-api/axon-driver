package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventsourcing.AbstractAggregateFactory;

public class DynamicAggregateFactory extends AbstractAggregateFactory<DynamicAggregate> {

  private final DynamicCommandProcessor dynamicCommandProcessor;

  public DynamicAggregateFactory(
      DynamicCommandProcessor dynamicCommandProcessor,
      DynamicAggregateModel dynamicAggregateModel
  ) {
    super(dynamicAggregateModel);
    this.dynamicCommandProcessor = dynamicCommandProcessor;
  }

  @Override
  protected DynamicAggregate doCreateAggregate(
      String aggregateIdentifier,
      DomainEventMessage firstEvent
  ) {
    return new DynamicAggregate(this.dynamicCommandProcessor, firstEvent.getType());
  }
}
