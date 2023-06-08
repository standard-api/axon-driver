package ai.stapi.axonsystem.configuration.implementations;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.springframework.stereotype.Service;
import org.jetbrains.annotations.NotNull;

public class CommandDispatchedAtInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

  public static final String DISPATCHED_AT_METADATA_KEY = "dispatchedAt";

  @NotNull
  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
      @NotNull List<? extends CommandMessage<?>> messages
  ) {
    return (index, command) -> {
      var stringDate = new Timestamp(System.currentTimeMillis()).toString();
      return command.andMetaData(
          Map.of(
              DISPATCHED_AT_METADATA_KEY, stringDate
          )
      );
    };
  }
}
