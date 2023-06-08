package ai.stapi.axonsystem.dynamic.messagehandler;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.HasHandlerAttributes;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@HasHandlerAttributes
public @interface DynamicMessageHandler {

  /**
   * Specifies the type of message that can be handled by the member method. Defaults to any
   * {@link Message}.
   */
  Class<? extends Message> messageType() default Message.class;

  /**
   * Specifies the type of message payload that can be handled by the member method. The payload of
   * the message should be assignable to this type. Defaults to any {@link Object}.
   */
  Class<?> payloadType() default Object.class;

  /**
   * Specifies the name of message payload that can be handled by the member method.
   */
  String messageName() default "";
}
