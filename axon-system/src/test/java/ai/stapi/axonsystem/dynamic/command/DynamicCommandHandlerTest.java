package ai.stapi.axonsystem.dynamic.command;

import ai.stapi.axonsystem.dynamic.command.testImplementations.SpyExampleDynamicCommandHandler;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.test.DomainTestCase;
import java.util.HashMap;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DynamicCommandHandlerTest extends DomainTestCase {

  @Autowired
  private CommandGateway commandGateway;

  @Autowired
  private SpyExampleDynamicCommandHandler spyExampleDynamicCommandHandler;

  @BeforeEach
  void before() {
    this.spyExampleDynamicCommandHandler.resetCounter();
  }

  @Test
  void itShouldNotReactToOtherCommand() {
    this.commandGateway.sendAndWait(
        new DynamicCommand(
            UniversallyUniqueIdentifier.randomUUID(),
            SpyExampleDynamicCommandHandler.OTHER_EXAMPLE_DYNAMIC_COMMAND_NAME,
            new HashMap<>()
        )
    );
    Assertions.assertEquals(
        0,
        this.spyExampleDynamicCommandHandler.getCallCounter()
    );
  }

  @Test
  void itShouldReactOnRightCommand() {
    this.commandGateway.sendAndWait(
        new DynamicCommand(
            UniversallyUniqueIdentifier.randomUUID(),
            SpyExampleDynamicCommandHandler.EXAMPLE_DYNAMIC_COMMAND_NAME,
            new HashMap<>()
        )
    );
    this.commandGateway.sendAndWait(
        new DynamicCommand(
            UniversallyUniqueIdentifier.randomUUID(),
            SpyExampleDynamicCommandHandler.EXAMPLE_DYNAMIC_COMMAND_NAME,
            new HashMap<>()
        )
    );
    Assertions.assertEquals(
        2,
        this.spyExampleDynamicCommandHandler.getCallCounter()
    );
  }
}