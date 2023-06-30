package ai.stapi.test;

import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateConfigurer;
import ai.stapi.graph.Graph;
import ai.stapi.graphsystem.messaging.event.Event;
import ai.stapi.graphsystem.messaging.event.GraphUpdatedEvent;
import ai.stapi.graphsystem.systemfixtures.model.SystemModelDefinitionsLoader;
import ai.stapi.test.schemaintegration.AbstractSchemaIntegrationTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import ai.stapi.utils.Retryable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventStream;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.Message;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(SystemModelDefinitionsLoader.SCOPE)
public abstract class AbstractDomainTestCase extends AbstractSchemaIntegrationTestCase {
  
  @Autowired
  private CommandGateway commandGateway;
  
  @Autowired
  private EmbeddedEventStore eventStore;

  @Autowired
  private EventStorageEngine eventStorageEngine;

  @Autowired
  private DynamicAggregateConfigurer dynamicAggregateConfigurer;

  private Instant testStartedAt;

  @BeforeEach
  @Order(1)
  protected void configureAggregates() {
    this.dynamicAggregateConfigurer.configureNewAggregates();
  }

  @BeforeEach
  protected void initStartedAt() {
    this.testStartedAt = Instant.now();
    if (this.eventStorageEngine instanceof TestInMemoryEventStorageEngine testStorage) {
      testStorage.reset();
    }
  }
  

  protected void givenCommandIsDispatched(Object command) {
    commandGateway.sendAndWait(command);
  }

  protected void whenCommandIsDispatched(Object command) {
    commandGateway.sendAndWait(command);

    if (command instanceof GenericCommandMessage<?> genericCommandMessage) {
      command = genericCommandMessage.getPayload();
    }
  }

  protected <T extends Event> T getLastEventOfType(Class<T> eventType) {
    return this.getLastEventOfType(eventType, 1);
  }

  protected <T extends Event> T getLastEventOfType(Class<T> eventType, int expectedNumberOfEvents) {
    List<Object> actualEventsOfType = Retryable.retry(100, 100, () -> {
      var actualEventStream = this.getActualEventStream();
      var eventsOfType = this.getActualEvents(actualEventStream).stream()
          .filter(o -> o.getClass().equals(eventType)).collect(Collectors.toList());

      return eventsOfType;
    }, expectedNumberOfEvents);

    Assertions.assertNotNull(actualEventsOfType, "No events found.");
    Assertions.assertEquals(expectedNumberOfEvents, actualEventsOfType.size(),
        "Not enough events found.");
    return (T) actualEventsOfType.get(actualEventsOfType.size() - 1);
  }
  protected void thenLastEventOfTypeGraphApproved(
      Class<? extends Event> eventType
  ) {
    this.thenLastEventOfTypeGraphApproved(eventType, 1);
  }

  protected void thenLastEventOfTypeGraphApproved(
      Class<? extends Event> eventType,
      int expectedNumberOfEvents
  ) {
    var uncheckedActualEvent = this.getLastEventOfType(eventType, expectedNumberOfEvents);
    Assertions.assertTrue(uncheckedActualEvent instanceof GraphUpdatedEvent);
    var actualEvent = (GraphUpdatedEvent) uncheckedActualEvent;
    this.thenGraphApproved(actualEvent.getSynchronizedGraph());
  }
  protected void thenMergedGraphOfAggregateApproved(String aggregateIdentifier) {
    var allEventMessages = this.getActualEventMessages(this.getActualEventStream());
    var mergedGraph = allEventMessages.stream()
        .filter(message -> message instanceof DomainEventMessage<?>)
        .map(message -> (DomainEventMessage<?>) message)
        .filter(message -> message.getAggregateIdentifier().equals(aggregateIdentifier))
        .map(Message::getPayload)
        .filter(payload -> payload instanceof GraphUpdatedEvent)
        .map(payload -> (GraphUpdatedEvent) payload)
        .reduce(
            new Graph(),
            this::reduceGraphEvent,
            Graph::merge
        );
    this.thenGraphApproved(mergedGraph);
  }

  private Graph reduceGraphEvent(
      Graph reduced,
      GraphUpdatedEvent event
  ) {
    return reduced
        .removeGraphElements(event.getGraphElementsForRemoval())
        .merge(event.getSynchronizedGraph());
  }
  
  private TrackingEventStream getActualEventStream() {
    var token = eventStore.createTokenAt(this.testStartedAt);
    return eventStore.openStream(token);
  }

  public List<Object> getActualEvents(TrackingEventStream actualEventStream) {
    var actualEvents = new ArrayList<>();
    while (actualEventStream.hasNextAvailable()) {
      actualEvents.add(getActualEvent(actualEventStream));
    }
    actualEventStream.close();
    return actualEvents;
  }

  private Object getActualEvent(TrackingEventStream actualEventStream) {
    try {
      return actualEventStream.nextAvailable().getPayload();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private List<TrackedEventMessage<?>> getActualEventMessages(
      TrackingEventStream actualEventStream
  ) {
    var actualEvents = new ArrayList<TrackedEventMessage<?>>();
    while (actualEventStream.hasNextAvailable()) {
      actualEvents.add(getActualEventMessage(actualEventStream));
    }
    actualEventStream.close();
    return actualEvents;
  }

  private TrackedEventMessage<?> getActualEventMessage(TrackingEventStream actualEventStream) {
    try {
      return actualEventStream.nextAvailable();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
