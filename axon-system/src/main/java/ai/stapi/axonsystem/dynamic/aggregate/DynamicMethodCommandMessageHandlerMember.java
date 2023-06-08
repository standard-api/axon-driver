package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import org.axonframework.messaging.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicMethodCommandMessageHandlerMember
    extends DynamicCommandMessageHandlerMember {

  public DynamicMethodCommandMessageHandlerMember(
      DynamicCommandProcessor dynamicCommandProcessor,
      String aggregateType,
      String commandName
  ) {
    super(dynamicCommandProcessor, aggregateType, commandName);
  }

  @Override
  public boolean isFactoryHandler() {
    return false;
  }


  @Override
  public Object handle(
      @NotNull Message<?> message,
      @Nullable DynamicAggregate target
  ) throws Exception {
    Objects.requireNonNull(target).handle((DynamicCommand) message.getPayload());
    return null;
  }

  @Override
  public <HT> Optional<HT> unwrap(Class<HT> handlerType) {
    if (handlerType.isAssignableFrom(Method.class)) {
      try {
        return Optional.of((HT) DynamicAggregate.class.getMethod("handle", DynamicCommand.class));
      } catch (NoSuchMethodException e) {
        return Optional.empty();
      }
    }
    if (handlerType.isAssignableFrom(this.getClass())) {
      return Optional.of((HT) this);
    }
    return Optional.empty();
  }


}