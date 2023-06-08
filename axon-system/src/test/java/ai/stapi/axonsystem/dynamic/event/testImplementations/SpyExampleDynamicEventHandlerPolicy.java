package ai.stapi.axonsystem.dynamic.event.testImplementations;

import ai.stapi.axonsystem.dynamic.event.DynamicEventHandler;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import org.springframework.stereotype.Service;

@Service
public class SpyExampleDynamicEventHandlerPolicy {

  public static final String EXAMPLE_DYNAMIC_EVENT_NAME = "ExampleDynamicEventName";
  private Integer callCounter = 0;

  @DynamicEventHandler(messageName = EXAMPLE_DYNAMIC_EVENT_NAME)
  public void on(DynamicGraphUpdatedEvent event) {
    this.callCounter = this.callCounter + 1;
  }

  public Integer getCallCounter() {
    return this.callCounter;
  }
}
