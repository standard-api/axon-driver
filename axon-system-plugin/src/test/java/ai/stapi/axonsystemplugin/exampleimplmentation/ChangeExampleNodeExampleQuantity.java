package ai.stapi.axonsystemplugin.exampleimplmentation;

import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import ai.stapi.identity.UniqueIdentifier;
import jakarta.annotation.Nonnull;

public class ChangeExampleNodeExampleQuantity extends AbstractCommand<UniqueIdentifier> {

  public static final String SERIALIZATION_TYPE = "ChangeExampleNodeExampleQuantity";

  @Nonnull
  private Integer newExampleQuantity;

  private ChangeExampleNodeExampleQuantity() {
  }

  public ChangeExampleNodeExampleQuantity(UniqueIdentifier exampleNodeId, Integer newExampleQuantity) {
    super(exampleNodeId, SERIALIZATION_TYPE);
    this.newExampleQuantity = newExampleQuantity;
  }

  public UniqueIdentifier getExampleNodeId() {
    return this.getTargetIdentifier();
  }

  public Integer getNewExampleQuantity() {
    return newExampleQuantity;
  }
}
