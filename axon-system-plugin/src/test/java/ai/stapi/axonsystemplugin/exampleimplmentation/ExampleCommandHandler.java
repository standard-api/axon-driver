package ai.stapi.axonsystemplugin.exampleimplmentation;

import ai.stapi.graph.Graph;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.stereotype.Service;

@Service
public class ExampleCommandHandler {
  
  private final EventGateway eventGateway;

  public ExampleCommandHandler(EventGateway eventGateway) {
    this.eventGateway = eventGateway;
  }

  @CommandHandler
  public void handle(ChangeExampleNodeExampleQuantity command) {
    if (command.getNewExampleQuantity().equals(50)) {
      throw new RuntimeException("Purposely failed!");
    }
    this.eventGateway.publish(
        new ExampleNodeExampleQuantityChanged(
            (UniversallyUniqueIdentifier) command.getExampleNodeId(),
            command.getNewExampleQuantity(),
            new Graph()
        )
    );
  }

  @CommandHandler
  public void handle(CreateNewExampleNodeCommand command) {
    this.eventGateway.publish(
        new ExampleNodeCreated(
            (UniversallyUniqueIdentifier) command.getExampleNodeId(),
            command.getExampleNodeType(),
            command.getExampleNodeName(),
            command.getExampleQuantity(),
            command.getExampleStringAttribute(),
            new Graph()
        )
    );
  }
}
