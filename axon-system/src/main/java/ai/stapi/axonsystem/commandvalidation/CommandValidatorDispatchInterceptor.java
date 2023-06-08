package ai.stapi.axonsystem.commandvalidation;

import ai.stapi.graphsystem.commandvalidation.model.exceptions.CannotDispatchCommand;
import ai.stapi.graphsystem.commandvalidation.model.CommandConstrainViolation;
import ai.stapi.graphsystem.commandvalidation.model.CommandValidator;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.graphsystem.messaging.command.Command;
import java.util.List;
import java.util.function.BiFunction;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class CommandValidatorDispatchInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

  private final CommandValidator commandValidator;

  public CommandValidatorDispatchInterceptor(CommandValidator commandValidator) {
    this.commandValidator = commandValidator;
  }

  @NotNull
  @Override
  public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(
      @NotNull List<? extends CommandMessage<?>> messages
  ) {
    return (index, commandMessage) -> {
      var payload = commandMessage.getPayload();
      if (!(payload instanceof Command)) {
        throw CannotDispatchCommand.becauseItDoesNotImplementCommandInterface(
            commandMessage.getCommandName(),
            payload.getClass()
        );
      }
      if (payload instanceof DynamicCommand command) {
        var violations = this.commandValidator.validate(command);
        if (this.isThereErrorViolation(violations)) {
          throw CannotDispatchCommand.becauseThereWereConstrainViolation(
              command.getSerializationType(),
              command.getTargetIdentifier().getId(),
              violations
          );
        }
      }
      return commandMessage;
    };
  }

  private boolean isThereErrorViolation(List<CommandConstrainViolation> violations) {
    return violations.stream().anyMatch(
        violation -> violation.getLevel().equals(CommandConstrainViolation.Level.ERROR)
    );
  }
}
