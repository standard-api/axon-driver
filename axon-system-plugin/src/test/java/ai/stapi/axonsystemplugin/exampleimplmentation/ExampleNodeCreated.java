package ai.stapi.axonsystemplugin.exampleimplmentation;

import ai.stapi.graph.Graph;
import ai.stapi.graphsystem.messaging.event.GraphUpdatedEvent;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import jakarta.annotation.Nonnull;


public class ExampleNodeCreated extends GraphUpdatedEvent {

  @Nonnull
  private UniversallyUniqueIdentifier exampleNodeId;

  @Nonnull
  private String exampleNodeType;

  @Nonnull
  private String exampleEntityName;

  @Nonnull
  private Integer exampleEntityQuantity;

  @Nonnull
  private String exampleEntityStringAttribute;

  private ExampleNodeCreated() {
  }

  public ExampleNodeCreated(
      @Nonnull UniversallyUniqueIdentifier exampleNodeId,
      @Nonnull String exampleNodeType,
      @Nonnull String exampleEntityName,
      @Nonnull Integer exampleEntityQuantity,
      @Nonnull String exampleEntityStringAttribute,
      Graph synchronizedGraph
  ) {
    super(synchronizedGraph);
    this.exampleNodeId = exampleNodeId;
    this.exampleNodeType = exampleNodeType;
    this.exampleEntityName = exampleEntityName;
    this.exampleEntityQuantity = exampleEntityQuantity;
    this.exampleEntityStringAttribute = exampleEntityStringAttribute;
  }

  public UniversallyUniqueIdentifier getExampleNodeId() {
    return exampleNodeId;
  }

  public String getExampleNodeType() {
    return exampleNodeType;
  }

  public String getExampleEntityName() {
    return exampleEntityName;
  }

  public Integer getExampleEntityQuantity() {
    return exampleEntityQuantity;
  }

  public String getExampleEntityStringAttribute() {
    return exampleEntityStringAttribute;
  }
}
