package ai.stapi.axonsystem.commandpersisting.exampleimplementation;

import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Service;

@Service
public class ExampleCommandHandler {
  
  @CommandHandler
  public void handle(ChangeExampleNodeExampleQuantity command) {
    throw new RuntimeException("Failed!");
  }

  @CommandHandler
  public void handle(CreateNewExampleNodeCommand command) {
    
  }
}
