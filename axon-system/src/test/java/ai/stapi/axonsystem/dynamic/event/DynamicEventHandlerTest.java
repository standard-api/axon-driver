package ai.stapi.axonsystem.dynamic.event;

import ai.stapi.axonsystem.dynamic.event.testImplementations.SpyExampleDynamicEventHandlerPolicy;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graph.Graph;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import ai.stapi.test.DomainTestCase;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DynamicEventHandlerTest extends DomainTestCase {

  @Autowired
  private SpyExampleDynamicEventHandlerPolicy spyExampleDynamicEventHandlerPolicy;

  @Autowired
  private EventGateway eventGateway;

  @Test
  void itShouldNotReactToOtherEvent() {
    this.eventGateway.publish(
        new DynamicGraphUpdatedEvent(
            "OtherExampleEventName",
            UniversallyUniqueIdentifier.randomUUID(),
            new Graph()
        )
    );
    Assertions.assertEquals(
        0,
        this.spyExampleDynamicEventHandlerPolicy.getCallCounter()
    );
  }

  @Test
  void itShouldReactOnRightEvent() {
    this.eventGateway.publish(
        new DynamicGraphUpdatedEvent(
            SpyExampleDynamicEventHandlerPolicy.EXAMPLE_DYNAMIC_EVENT_NAME,
            UniversallyUniqueIdentifier.randomUUID(),
            new Graph()
        )
    );
    this.eventGateway.publish(
        new DynamicGraphUpdatedEvent(
            SpyExampleDynamicEventHandlerPolicy.EXAMPLE_DYNAMIC_EVENT_NAME,
            UniversallyUniqueIdentifier.randomUUID(),
            new Graph()
        )
    );
    Assertions.assertEquals(
        2,
        this.spyExampleDynamicEventHandlerPolicy.getCallCounter()
    );
  }

}