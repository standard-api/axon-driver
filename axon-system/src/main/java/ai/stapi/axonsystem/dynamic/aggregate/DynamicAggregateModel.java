package ai.stapi.axonsystem.dynamic.aggregate;

import static java.lang.String.format;

import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.axonframework.commandhandling.CommandMessageHandlingMember;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.MessageHandlerInvocationException;
import org.axonframework.messaging.annotation.MessageHandlingMember;
import org.axonframework.messaging.annotation.MessageInterceptingMember;
import org.axonframework.modelling.command.inspection.AggregateModel;
import org.axonframework.modelling.command.inspection.EntityModel;

public class DynamicAggregateModel implements AggregateModel<DynamicAggregate> {

  private final String aggregateType;
  private final Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>>
      allCommandHandlerInterceptors;
  private final Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>>
      allCommandHandlers;
  private final Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>>
      allEventHandlers;

  public DynamicAggregateModel(
      String aggregateType,
      Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>> allCommandHandlerInterceptors,
      Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>> allCommandHandlers,
      Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>> allEventHandlers
  ) {
    this.aggregateType = aggregateType;
    this.allCommandHandlerInterceptors = allCommandHandlerInterceptors;
    this.allCommandHandlers = allCommandHandlers;
    this.allEventHandlers = allEventHandlers;
  }

  @Override
  public String type() {
    return this.aggregateType;
  }

  @Override
  public Long getVersion(DynamicAggregate dynamicAggregate) {
    return dynamicAggregate.getVersion();
  }

  @Override
  public Object getIdentifier(DynamicAggregate dynamicAggregate) {
    return dynamicAggregate.getIdentity();
  }

  @Override
  public String routingKey() {
    return "identity";
  }

  @Override
  public void publish(
      EventMessage<?> eventMessage,
      DynamicAggregate dynamicAggregate
  ) {
    this.getHandler(eventMessage).ifPresent(h -> {
      try {
        //TODO: make chained interceptors, see HandlerInspector in regular model
        h.handle(eventMessage, dynamicAggregate);
      } catch (Exception e) {
        String eventName;
        if (eventMessage.getPayloadType().equals(DynamicGraphUpdatedEvent.class)) {
          var payload = (DynamicGraphUpdatedEvent) eventMessage.getPayload();
          eventName = payload.getEventName();
        } else {
          eventName = eventMessage.getPayloadType().toString();
        }
        throw new MessageHandlerInvocationException(
            format(
                "Error handling event of type [%s] in aggregate [%s] with id [%s]",
                eventName,
                this.aggregateType,
                this.getIdentifier(dynamicAggregate)
            ),
            e
        );
      }
    });
  }

  @Override
  public <C> EntityModel<C> modelOf(Class<? extends C> childEntityType) {
    throw new RuntimeException("Dynamic Aggregate model cannot create other models.");
  }

  @Override
  public Class<? extends DynamicAggregate> entityClass() {
    return DynamicAggregate.class;
  }

  @Override
  public Optional<Class<?>> type(String declaredType) {
    if (declaredType.equals(this.aggregateType)) {
      return Optional.of(DynamicAggregate.class);
    }
    return Optional.empty();
  }

  @Override
  public Optional<String> declaredType(Class<?> type) {
    return Optional.of(this.aggregateType);
  }

  @Override
  public Stream<Class<?>> types() {
    return Stream.of(DynamicAggregate.class);
  }

  @Override
  public Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>> allCommandHandlers() {
    return Collections.unmodifiableMap(this.allCommandHandlers);
  }

  @Override
  public Stream<MessageHandlingMember<? super DynamicAggregate>> commandHandlers(
      Class<? extends DynamicAggregate> type
  ) {
    return this.filterHandlers(this.allCommandHandlers, type);
  }

  @Override
  public Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>> allCommandHandlerInterceptors() {
    return Collections.unmodifiableMap(this.allCommandHandlerInterceptors);
  }

  @Override
  public Stream<MessageHandlingMember<? super DynamicAggregate>> commandHandlerInterceptors(
      Class<? extends DynamicAggregate> type
  ) {
    return this.filterHandlers(this.allCommandHandlerInterceptors, type);
  }

  @Override
  public Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>> allEventHandlers() {
    return Collections.unmodifiableMap(this.allEventHandlers);
  }

  private Optional<MessageHandlingMember<? super DynamicAggregate>> getHandler(Message<?> message) {
    return this.filterHandlers(this.allEventHandlers, DynamicAggregate.class)
        .filter(handler -> handler.canHandle(message))
        .findFirst();
  }

  private Stream<MessageHandlingMember<? super DynamicAggregate>> filterHandlers(
      Map<Class<?>, List<MessageHandlingMember<? super DynamicAggregate>>> handlers,
      Class<?> subtype
  ) {
    Class<?> type = subtype;
    while (!handlers.containsKey(type) && !Objects.equals(type, Object.class)
        && type.getSuperclass() != null) {
      type = type.getSuperclass();
    }
    return handlers.getOrDefault(type, Collections.emptyList()).stream();
  }

  public static class Builder {

    private final String aggregateType;
    private final List<MessageHandlingMember<? super DynamicAggregate>>
        allCommandHandlerInterceptors;
    private final List<MessageHandlingMember<? super DynamicAggregate>> allCommandHandlers;
    private final List<MessageHandlingMember<? super DynamicAggregate>> allEventHandlers;

    public Builder(String aggregateType) {
      this.aggregateType = aggregateType;
      this.allCommandHandlerInterceptors = new ArrayList<>();
      this.allCommandHandlers = new ArrayList<>();
      this.allEventHandlers = new ArrayList<>();
    }

    public Builder addCommandHandler(
        CommandMessageHandlingMember<? super DynamicAggregate> commandHandler
    ) {
      this.allCommandHandlers.add(commandHandler);
      return this;
    }

    public Builder addCommandHandlerInterceptor(
        MessageInterceptingMember<? super DynamicAggregate> commandHandlerInterceptor
    ) {
      this.allCommandHandlerInterceptors.add(commandHandlerInterceptor);
      return this;
    }

    public Builder addEventHandler(
        MessageHandlingMember<? super DynamicAggregate> eventHandler
    ) {
      this.allEventHandlers.add(eventHandler);
      return this;
    }

    public DynamicAggregateModel build() {
      return new DynamicAggregateModel(
          this.aggregateType,
          new HashMap<>(Map.of(DynamicAggregate.class, this.allCommandHandlerInterceptors)),
          new HashMap<>(Map.of(DynamicAggregate.class, this.allCommandHandlers)),
          new HashMap<>(Map.of(DynamicAggregate.class, this.allEventHandlers))
      );
    }
  }
}
