package ai.stapi.axonsystem.dynamic.aggregate.testImplementations;

import ai.stapi.graph.graphElementForRemoval.GraphElementForRemoval;
import ai.stapi.graph.Graph;
import ai.stapi.graphsystem.genericGraphEventFactory.specific.SpecificGraphEventFactory;
import ai.stapi.graphsystem.messaging.event.AggregateGraphUpdatedEvent;
import ai.stapi.identity.UniqueIdentifier;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class ExampleDynamicAggregateCreatedFactory implements SpecificGraphEventFactory {

  @Override
  public AggregateGraphUpdatedEvent<? extends UniqueIdentifier> createEvent(
      UniqueIdentifier identity,
      Graph graph,
      List<GraphElementForRemoval> elementsForRemoval
  ) {
    return new ExampleDynamicAggregateCreated(identity, graph);
  }

  @Override
  public boolean supports(
      Class<? extends AggregateGraphUpdatedEvent<? extends UniqueIdentifier>> event) {
    return event.equals(ExampleDynamicAggregateCreated.class);
  }

  public static class ExampleDynamicAggregateCreated
      extends AggregateGraphUpdatedEvent<UniqueIdentifier> {

    protected ExampleDynamicAggregateCreated(
        UniqueIdentifier identity,
        Graph synchronizedGraph
    ) {
      super(identity, synchronizedGraph);
    }
  }
}
