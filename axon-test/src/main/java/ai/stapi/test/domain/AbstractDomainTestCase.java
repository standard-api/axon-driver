package ai.stapi.test.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.stapi.graphsystem.messaging.command.Command;
import ai.stapi.graphsystem.messaging.command.EndpointCommand;
import ai.stapi.graphsystem.systemfixtures.model.SystemModelDefinitionsLoader;
import ai.stapi.test.base.AbstractAxonTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import java.util.Map;
import java.util.Optional;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.MetaData;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(SystemModelDefinitionsLoader.SCOPE)
public abstract class AbstractDomainTestCase extends AbstractAxonTestCase {
  
  @Autowired
  private CommandGateway commandGateway;

  private static boolean validCommandClass(Object deserializedObject) {
    return deserializedObject instanceof Command || deserializedObject instanceof Map<?, ?>;
  }

  protected void givenCommandIsDispatched(Object command) {
    commandGateway.sendAndWait(command);
  }
  

  protected void whenCommandIsDispatched(Object command) {
    commandGateway.sendAndWait(command);

    if (command instanceof GenericCommandMessage<?> genericCommandMessage) {
      command = genericCommandMessage.getPayload();
    }
    this.thenCommandCanBeSerialized(command);
  }

  protected void whenCommandIsDispatched(Object command, MetaData metaData) {
    commandGateway.sendAndWait(command, metaData);

    this.thenCommandCanBeSerialized(command);
  }

  protected Optional<CommandExecutionException> whenCommandThrowingExceptionIsDispatched(
      Object command) {
    try {
      commandGateway.sendAndWait(command);
    } catch (CommandExecutionException e) {
      return Optional.of(e);
    }
    return Optional.empty();
  }

  private void thenCommandCanBeSerialized(Object commandToSerialize) {
    this.thenCommandCanBeSerializedWithXSStream(commandToSerialize);
    if (commandToSerialize instanceof EndpointCommand) {
      this.thenCommandCanBeSerializedWithJsonSerializer(commandToSerialize);
    }

  }

  private void thenCommandCanBeSerializedWithXSStream(Object commandToSerialize) {
    var serializedCommand = this.serializer.serialize(
        commandToSerialize,
        String.class
    );
    var deserializedObject = this.serializer.deserialize(serializedCommand);
    assertTrue(
        validCommandClass(deserializedObject),
        deserializedObject.getClass() + " does not implement command interface."
    );
    Assertions.assertEquals(commandToSerialize.getClass(), deserializedObject.getClass());
    //TODO: Make working equals
  }

  private void thenCommandCanBeSerializedWithJsonSerializer(Object commandToSerialize) {
    var serializedCommand = this.serializer.serialize(commandToSerialize, String.class);
    var deserializedObject = this.serializer.deserialize(serializedCommand);
    assertTrue(
        validCommandClass(deserializedObject),
        deserializedObject.getClass() + " does not implement Command interface."
    );
    var deserializedCommand = (Command) deserializedObject;
    Assertions.assertEquals(commandToSerialize.getClass(), deserializedCommand.getClass());
    //TODO: Make working equals
  }
}
