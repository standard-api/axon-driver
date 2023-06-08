package ai.stapi.axonsystem.dynamic.command;

import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.modelling.command.AnnotationCommandTargetResolver;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.modelling.command.VersionedAggregateIdentifier;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class AbstractCommandTargetResolver implements CommandTargetResolver {
  
  private final AnnotationCommandTargetResolver annotationCommandTargetResolver;

  public AbstractCommandTargetResolver() {
    this.annotationCommandTargetResolver = AnnotationCommandTargetResolver.builder().build();
  }

  @Override
  public VersionedAggregateIdentifier resolveTarget(@NotNull CommandMessage<?> command) {
    var payload = command.getPayload();
    if (payload instanceof AbstractCommand<?> abstractCommand) {
      return new VersionedAggregateIdentifier(
          abstractCommand.getTargetIdentifier(),
          null
      );
    }
    return this.annotationCommandTargetResolver.resolveTarget(command);
  }
}
