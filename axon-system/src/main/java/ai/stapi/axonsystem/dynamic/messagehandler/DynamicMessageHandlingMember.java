package ai.stapi.axonsystem.dynamic.messagehandler;

import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.util.Map;
import java.util.Optional;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.AnnotatedMessageHandlingMember;
import org.axonframework.messaging.annotation.MessageHandlingMember;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicMessageHandlingMember<T> implements MessageHandlingMember<T> {

  private final AnnotatedMessageHandlingMember<T> annotatedMessageHandlingMember;
  private final String messageName;

  public DynamicMessageHandlingMember(
      Executable executable,
      Class<? extends Message<?>> messageType,
      Class<?> explicitPayloadType,
      ParameterResolverFactory parameterResolverFactory,
      String messageName
  ) {
    this.messageName = messageName;
    this.annotatedMessageHandlingMember = new AnnotatedMessageHandlingMember<>(
        executable,
        messageType,
        explicitPayloadType,
        parameterResolverFactory
    );
  }

  @Override
  public Class<?> payloadType() {
    return this.annotatedMessageHandlingMember.payloadType();
  }

  @Override
  public boolean canHandle(@NotNull Message<?> message) {
    if (!this.annotatedMessageHandlingMember.canHandle(message)) {
      return false;
    }
    if (message.getPayloadType().equals(DynamicGraphUpdatedEvent.class)) {
      var dynamicEvent = (DynamicGraphUpdatedEvent) message.getPayload();
      return dynamicEvent.getEventName().equals(this.messageName);
    }
    //TODO: Same thing for query
    return false;
  }

  @Override
  public boolean canHandleMessageType(@NotNull Class<? extends Message> messageType) {
    return this.annotatedMessageHandlingMember.canHandleMessageType(messageType);
  }

  @Override
  public Object handle(@NotNull Message<?> message, @Nullable T target) throws Exception {
    return this.annotatedMessageHandlingMember.handle(message, target);
  }

  @Override
  public <HT> Optional<HT> unwrap(Class<HT> handlerType) {
    return this.annotatedMessageHandlingMember.unwrap(handlerType);
  }

  @Override
  @Deprecated
  public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
    return this.annotatedMessageHandlingMember.hasAnnotation(annotationType);
  }

  @Override
  @Deprecated
  public Optional<Map<String, Object>> annotationAttributes(
      Class<? extends Annotation> annotationType) {
    return this.annotatedMessageHandlingMember.annotationAttributes(annotationType);
  }

  @Override
  public int priority() {
    return this.annotatedMessageHandlingMember.priority();
  }

  @Override
  public boolean canHandleType(@NotNull Class<?> payloadType) {
    return this.annotatedMessageHandlingMember.canHandleType(payloadType);
  }

  @Override
  public Class<?> declaringClass() {
    return this.annotatedMessageHandlingMember.declaringClass();
  }

  @Override
  public String signature() {
    return this.annotatedMessageHandlingMember.signature();
  }

  @Override
  public <R> Optional<R> attribute(String attributeKey) {
    return this.annotatedMessageHandlingMember.attribute(attributeKey);
  }
}
