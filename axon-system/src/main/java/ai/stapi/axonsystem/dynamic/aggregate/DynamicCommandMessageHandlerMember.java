package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandMessageHandlingMember;
import org.axonframework.messaging.Message;
import org.jetbrains.annotations.NotNull;

public abstract class DynamicCommandMessageHandlerMember
    implements CommandMessageHandlingMember<DynamicAggregate> {

  protected final DynamicCommandProcessor dynamicCommandProcessor;
  protected final String aggregateType;
  private final String commandName;

  protected DynamicCommandMessageHandlerMember(
      DynamicCommandProcessor dynamicCommandProcessor,
      String aggregateType,
      String commandName
  ) {
    this.commandName = commandName;
    this.aggregateType = aggregateType;
    this.dynamicCommandProcessor = dynamicCommandProcessor;
  }

  @Override
  public String commandName() {
    return this.commandName;
  }

  @Override
  public String routingKey() {
    return DynamicCommand.TARGET_IDENTIFIER_FIELD_NAME;
  }

  @Override
  public Class<?> payloadType() {
    return DynamicCommand.class;
  }

  @Override
  public boolean canHandle(@NotNull Message<?> message) {
    if (!(message instanceof CommandMessage<?> commandMessage)) {
      return false;
    }
    if (!(commandMessage.getPayload() instanceof DynamicCommand dynamicCommand)) {
      return false;
    }
    return dynamicCommand.getSerializationType().equals(this.commandName);
  }

  @Override
  public boolean canHandleMessageType(@NotNull Class<? extends Message> messageType) {
    return messageType.isAssignableFrom(DynamicCommand.class);
  }

  @Override
  @Deprecated
  public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
    return false;
  }

  @Override
  @Deprecated
  public Optional<Map<String, Object>> annotationAttributes(
      Class<? extends Annotation> annotationType) {
    return Optional.empty();
  }
}
