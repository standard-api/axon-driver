package ai.stapi.axonsystem.graphaggregate;

import ai.stapi.graph.graphElementForRemoval.GraphElementForRemoval;
import ai.stapi.graph.inMemoryGraph.InMemoryGraphRepository;
import ai.stapi.graphsystem.messaging.event.GraphUpdatedEvent;
import org.axonframework.eventsourcing.EventSourcingHandler;

public abstract class AggregateWithGraph {

  protected final InMemoryGraphRepository inMemoryGraphRepository =
      new InMemoryGraphRepository();

  @EventSourcingHandler
  public void onEveryGraphEvent(GraphUpdatedEvent event) {
    this.inMemoryGraphRepository.removeGraphElements(
        event.getGraphElementsForRemoval()
            .toArray(new GraphElementForRemoval[0])
    );
    this.inMemoryGraphRepository.merge(event.getSynchronizedGraph());
  }
}
