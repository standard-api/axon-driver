package ai.stapi.axonsystem.configuration.implementations;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;

public class CustomFailureLoggingCallback<C, R> implements CommandCallback<C, R> {

  private final CommandCallback<C, R> delegate;
  private final Logger logger;

  public CustomFailureLoggingCallback(Logger logger) {
    this.logger = logger;
    this.delegate = null;
  }

  @Override
  public void onResult(
      @Nonnull CommandMessage<? extends C> commandMessage,
      @Nonnull CommandResultMessage<? extends R> commandResultMessage
  ) {
    commandResultMessage.optionalExceptionResult().ifPresent(cause ->
        logger.log(
            Level.WARNING,
            String.format(
                "Command '%s' resulted in %s",
                commandMessage.getCommandName(),
                cause.getClass().getSimpleName()
            ),
            cause
        )
    );
    if (delegate != null) {
      delegate.onResult(commandMessage, commandResultMessage);
    }
  }
}
