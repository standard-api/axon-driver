package ai.stapi.axonsystem.dynamic.messagehandler;

import static org.axonframework.common.annotation.AnnotationUtils.findAnnotationAttributes;

import java.lang.reflect.Executable;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.MessageHandlingMember;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.springframework.stereotype.Service;

@Service
public class DynamicMessageHandlingMemberDefinition implements HandlerDefinition {

  @Override
  public <T> Optional<MessageHandlingMember<T>> createHandler(
      @Nonnull Class<T> declaringType,
      @Nonnull Executable executable,
      @Nonnull ParameterResolverFactory parameterResolverFactory
  ) {
    return findAnnotationAttributes(executable, DynamicMessageHandler.class).map(
        attr -> new DynamicMessageHandlingMember<T>(
            executable,
            (Class<? extends Message<?>>) attr.getOrDefault("messageType", Message.class),
            (Class<?>) attr.getOrDefault("payloadType", Object.class),
            parameterResolverFactory,
            (String) attr.get("messageName")
        )
    );
  }
}
