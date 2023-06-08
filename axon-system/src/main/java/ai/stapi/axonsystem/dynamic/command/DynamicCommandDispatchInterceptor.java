package ai.stapi.axonsystem.dynamic.command;

import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import java.util.List;
import java.util.function.BiFunction;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.messaging.GenericMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jetbrains.annotations.NotNull;

public class DynamicCommandDispatchInterceptor  implements MessageDispatchInterceptor<CommandMessage<?>> {

  @NotNull
  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
      @NotNull List<? extends CommandMessage<?>> messages
  ) {
    return (integer, commandMessage) -> {
      if (commandMessage.getPayload() instanceof DynamicCommand dynamicCommand) {
        return new GenericCommandMessage<>(
            new GenericMessage<>(dynamicCommand),
            dynamicCommand.getSerializationType()
        );
      }
      return commandMessage;
    };
  }
}
