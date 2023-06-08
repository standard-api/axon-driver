package ai.stapi.axonsystemplugin.exampleimplmentation;

import ai.stapi.graph.Graph;
import ai.stapi.graphsystem.messaging.event.GraphUpdatedEvent;
import ai.stapi.identity.UniversallyUniqueIdentifier;

public class ExampleNodeExampleQuantityChanged extends GraphUpdatedEvent {

  private UniversallyUniqueIdentifier exampleNodeId;

  private Integer newExampleQuantity;

  private ExampleNodeExampleQuantityChanged() {
  }

  public ExampleNodeExampleQuantityChanged(
      UniversallyUniqueIdentifier exampleNodeId,
      Integer newExampleQuantity,
      Graph synchronizedGraph
  ) {
    super(synchronizedGraph);
    this.exampleNodeId = exampleNodeId;
    this.newExampleQuantity = newExampleQuantity;
  }

  public UniversallyUniqueIdentifier getExampleEntityId() {
    return exampleNodeId;
  }

  public Integer getNewExampleQuantity() {
    return newExampleQuantity;
  }
}
