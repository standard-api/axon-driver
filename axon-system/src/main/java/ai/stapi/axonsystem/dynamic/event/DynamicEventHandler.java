package ai.stapi.axonsystem.dynamic.event;

import ai.stapi.axonsystem.dynamic.messagehandler.DynamicMessageHandler;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.axonframework.eventhandling.EventMessage;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@DynamicMessageHandler(
    messageType = EventMessage.class,
    payloadType = DynamicGraphUpdatedEvent.class
)
public @interface DynamicEventHandler {

  /**
   * Specifies the name of message that can be handled by the member method.
   */
  String messageName();

}
