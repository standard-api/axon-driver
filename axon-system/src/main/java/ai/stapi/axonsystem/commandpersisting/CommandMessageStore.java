package ai.stapi.axonsystem.commandpersisting;

import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import ai.stapi.graphsystem.messaging.command.AbstractPayloadCommand;
import java.util.HashMap;
import java.util.List;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;

public interface CommandMessageStore {

  void storeCommand(CommandMessage<?> commandMessage);

  List<PersistedCommandMessage<?>> getAll();

  void wipeAll();

  default String extractId(CommandMessage<?> commandMessage) {
    var routingStrategy = AnnotationRoutingStrategy.builder().build();
    var payload = commandMessage.getPayload();
    if (payload instanceof AbstractCommand<?> abstractCommand) {
      return abstractCommand.getTargetIdentifier().getId();
    } else {
      return routingStrategy.getRoutingKey(commandMessage);
    }
  }

  default Object extractPayload(CommandMessage<?> commandMessage) {
    var payload = commandMessage.getPayload();
    if (payload instanceof AbstractPayloadCommand<?> abstractCreateResourceCommand) {
      var finalPayload = new HashMap<>(abstractCreateResourceCommand.getData());
      finalPayload.put(
          "id",
          abstractCreateResourceCommand.getTargetIdentifier().getId()
      );
      payload = finalPayload;
    }
    return payload;
  }
}
