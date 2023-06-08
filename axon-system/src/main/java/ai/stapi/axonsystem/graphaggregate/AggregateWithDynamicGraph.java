package ai.stapi.axonsystem.graphaggregate;

import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import ai.stapi.graphoperations.objectGraphMapper.model.MissingFieldResolvingStrategy;
import ai.stapi.graphsystem.messaging.event.AggregateGraphUpdatedEvent;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateVersion;

public abstract class AggregateWithDynamicGraph<T extends UniqueIdentifier>  extends AggregateWithGraph {

  @AggregateIdentifier
  protected T identity;

  @AggregateVersion
  protected long version;
  
  protected DynamicCommandProcessor commandProcessor;

  protected AggregateWithDynamicGraph() {
  }

  protected AggregateWithDynamicGraph(DynamicCommandProcessor commandProcessor) {
    this.commandProcessor = commandProcessor;
  }

  protected void processCommandDynamically(AbstractCommand<T> command) {
    this.processCommandDynamically(command, MissingFieldResolvingStrategy.LENIENT);
  }

  protected void processCommandDynamically(
      AbstractCommand<T> command,
      MissingFieldResolvingStrategy missingFieldResolvingStrategy
  ) {
    this.commandProcessor.processCommand(
        command,
        this.inMemoryGraphRepository.getGraph(),
        missingFieldResolvingStrategy
    ).forEach(AggregateLifecycle::apply);
  }

  protected void onAggregateCreated(AggregateGraphUpdatedEvent<T> event) {
    this.identity = event.getIdentity();
    this.onEveryGraphEvent(event);
  }

  public T getIdentity() {
    return identity;
  }

  public long getVersion() {
    return version;
  }
}
