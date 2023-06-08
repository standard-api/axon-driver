package ai.stapi.axonsystem.dynamic.command.testImplementations;

import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import org.axonframework.commandhandling.CommandHandler;
import org.springframework.stereotype.Service;

@Service
public class SpyExampleDynamicCommandHandler {

  public static final String EXAMPLE_DYNAMIC_COMMAND_NAME = "ExampleDynamicCommandName";
  public static final String OTHER_EXAMPLE_DYNAMIC_COMMAND_NAME = "OtherExampleCommandName";
  private Integer callCounter = 0;

  @CommandHandler(commandName = EXAMPLE_DYNAMIC_COMMAND_NAME)
  public void handle(DynamicCommand command) {
    this.callCounter = this.callCounter + 1;
  }

  @CommandHandler(commandName = OTHER_EXAMPLE_DYNAMIC_COMMAND_NAME)
  public void handleOther(DynamicCommand command) {
  }

  public Integer getCallCounter() {
    return callCounter;
  }

  public void resetCounter() {
    this.callCounter = 0;
  }
}
