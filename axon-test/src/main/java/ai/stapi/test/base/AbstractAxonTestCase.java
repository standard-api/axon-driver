package ai.stapi.test.base;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.stapi.axonsystem.dynamic.DynamicAxonConfigurer;
import ai.stapi.graph.Graph;
import ai.stapi.graph.graphElementForRemoval.GraphElementForRemoval;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graphsystem.messaging.event.Event;
import ai.stapi.graphsystem.messaging.event.GraphUpdatedEvent;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.objectRenderer.infrastructure.objectToJsonStringRenderer.ObjectToJSonStringOptions;
import ai.stapi.test.domain.TestInMemoryEventStorageEngine;
import ai.stapi.test.schemaintegration.AbstractSchemaIntegrationTestCase;
import ai.stapi.utils.LineFormatter;
import ai.stapi.utils.Retryable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.axonframework.eventhandling.DomainEventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventStream;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.MetaData;
import org.axonframework.serialization.Serializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class AbstractAxonTestCase extends AbstractSchemaIntegrationTestCase {

  @Autowired
  @Qualifier("messageSerializer")
  protected Serializer serializer;

  @Autowired
  private TokenStore tokenstore;

  @Autowired
  private EmbeddedEventStore eventStore;

  @Autowired
  private EventStorageEngine eventStorageEngine;

  @Autowired
  private DynamicAxonConfigurer dynamicAxonConfigurer;

  private Instant testStartedAt;

  @BeforeEach
  @Order(1)
  protected void configureAggregates() {
    this.dynamicAxonConfigurer.configureNewAggregates();
  }

  @BeforeEach
  protected void initStartedAt() {
    this.testStartedAt = Instant.now();
    if (this.eventStorageEngine instanceof TestInMemoryEventStorageEngine testStorage) {
      testStorage.reset();
    }
  }

  protected void thenExpectedEventTypesSaved(Class<?>... expectedEventTypes) {
    this.thenExpectedEventTypesSaved(Arrays.stream(expectedEventTypes).toList());
  }

  protected void thenExpectedEventTypesSaved(List<Class<?>> expectedEventTypes) {

    int retries = 20;
    int expectedEventCount = expectedEventTypes.size();
    List<Class<?>> actualEventTypes = Retryable.retry(retries, 200, () -> {
      List<Class<?>> eventTypes = this.getActualEventTypes(this.getActualEventStream());
      return eventTypes;
    }, expectedEventCount);
//
    Assertions.assertEquals(
        expectedEventTypes.size(),
        actualEventTypes.size(),
        this.getErrorMessage(expectedEventTypes, actualEventTypes)
    );
    assertEventTypeListsAreSame(expectedEventTypes, actualEventTypes);
    this.thenSavedEventsCanBeSerialized();
  }

  protected void thenExpectedDynamicEventsSaved(String... expectedEventNames) {
    this.thenExpectedDynamicEventsSaved(Arrays.stream(expectedEventNames).toList());
  }

  protected void thenExpectedDynamicEventsSaved(List<String> expectedEventNames) {

    var actualEventsOfType = Retryable.retry(
        100, 100, () -> {
          var actualEventStream = this.getActualEventStream();
          var eventsOfType = this.getActualEvents(actualEventStream).stream()
              .filter(o -> o instanceof DynamicGraphUpdatedEvent)
              .map(o -> (DynamicGraphUpdatedEvent) o)
              .collect(Collectors.toList());

          return eventsOfType;
        },
        expectedEventNames.size()
    );

    Assertions.assertEquals(
        expectedEventNames.size(),
        actualEventsOfType.size(),
        this.getDynamicEventsErrorMessage(expectedEventNames, actualEventsOfType)
    );
    this.assertDynamicEventsHaveCorrectNames(expectedEventNames, actualEventsOfType);
    this.thenSavedEventsCanBeSerialized();
  }

  protected <T extends Event> T getLastEventOfType(Class<T> eventType) {
    return this.getLastEventOfType(eventType, 1);
  }

  protected DynamicGraphUpdatedEvent getLastDynamicEventOfName(String eventName) {
    return this.getLastDynamicEventOfName(eventName, 1);
  }

  protected DynamicGraphUpdatedEvent getLastDynamicEventOfName(
      String eventName,
      int expectedNumberOfEvents
  ) {
    var actualEventsOfType = Retryable.retry(
        100, 100, () -> {
          var actualEventStream = this.getActualEventStream();
          var eventsOfType = this.getActualEvents(actualEventStream).stream()
              .filter(o -> o instanceof DynamicGraphUpdatedEvent)
              .map(o -> (DynamicGraphUpdatedEvent) o)
              .filter(dynamicGraphUpdatedEvent -> dynamicGraphUpdatedEvent.getEventName()
                  .equals(eventName))
              .collect(Collectors.toList());

          return eventsOfType;
        },
        expectedNumberOfEvents
    );

    Assertions.assertNotNull(actualEventsOfType, "No events found.");
    Assertions.assertEquals(
        expectedNumberOfEvents,
        actualEventsOfType.size(),
        "Not enough events found."
    );
    return actualEventsOfType.get(actualEventsOfType.size() - 1);
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

  protected MetaData getMetaDataForLastEventOfType(Class<? extends Event> eventType) {
    var metaData = this.getActualEventStream().asStream()
        .filter(eventMessage -> eventMessage.getPayloadType().equals(eventType))
        .map(Message::getMetaData)
        .toList();
    Assertions.assertTrue(metaData.size() > 0, "No such events found.");
    return metaData.get(metaData.size() - 1);
  }

  protected <T extends Event> Optional<T> getLastEventOfTypeOptionally(Class<T> eventType) {
    var actualEventStream = this.getActualEventStream();
    return this.getActualEvents(actualEventStream).stream()
        .filter(o -> o.getClass().equals(eventType))
        .map(event -> (T) event)
        .findAny();
  }

  protected void thenLastEventElementsForRemovalApproved() {
    var actualEventStream = this.getActualEventStream();
    var actualEvents = this.getActualEvents(actualEventStream);
    var uncheckedActualEvent = actualEvents.get(actualEvents.size() - 1);
    Assertions.assertTrue(uncheckedActualEvent instanceof GraphUpdatedEvent);
    var actualEvent = (GraphUpdatedEvent) uncheckedActualEvent;
    this.approveElementsForRemoval(actualEvent.getGraphElementsForRemoval());
  }

  protected void thenLastEventElementsForRemovalApproved(Class<? extends Event> eventType) {
    var uncheckedActualEvent = this.getLastEventOfType(eventType);
    Assertions.assertTrue(uncheckedActualEvent instanceof GraphUpdatedEvent);
    var actualEvent = (GraphUpdatedEvent) uncheckedActualEvent;
    this.approveElementsForRemoval(actualEvent.getGraphElementsForRemoval());
  }

  protected void thenLastEventGraphApproved() {
    var actualEventStream = this.getActualEventStream();
    var actualEvents = this.getActualEvents(actualEventStream);
    var uncheckedActualEvent = actualEvents.get(actualEvents.size() - 1);
    Assertions.assertTrue(uncheckedActualEvent instanceof GraphUpdatedEvent);
    var actualEvent = (GraphUpdatedEvent) uncheckedActualEvent;
    this.thenGraphApproved(actualEvent.getSynchronizedGraph());
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

  protected void thenLastDynamicEventOfNameApproved(
      String eventName
  ) {
    var actualEvent = this.getLastDynamicEventOfName(eventName);
    this.thenGraphApproved(actualEvent.getSynchronizedGraph());
  }

  protected void thenLastDynamicEventOfNameApproved(
      String eventName,
      int expectedNumberOfEvents
  ) {
    var actualEvent = this.getLastDynamicEventOfName(
        eventName,
        expectedNumberOfEvents
    );
    this.thenGraphApproved(actualEvent.getSynchronizedGraph());
  }

  protected void thenMergedGraphOfAggregateApproved(UniqueIdentifier aggregateIdentifier) {
    this.thenMergedGraphOfAggregateApproved(aggregateIdentifier.getId());
  }

  protected List<TrackedEventMessage<?>> getAllTrackedMessagesWithPayloadType(
      Class<? extends Event> eventType) {
    var eventMessages = this.getActualEventMessages(this.getActualEventStream());
    return eventMessages.stream()
        .filter(message -> message.getPayloadType().equals(eventType))
        .toList();
  }

  protected <T extends Event> TrackedEventMessage<T> getLastEventMessagesOfPayloadType(
      Class<T> eventType) {
    var expectedNumberOfMessages = 1;
    List<TrackedEventMessage<?>> actualEvents = Retryable.retry(
        20,
        100,
        () -> {
          List<TrackedEventMessage<?>> currentActualEvents = this.getActualEventMessages(
                  this.getActualEventStream()
              )
              .stream()
              .filter(message -> message.getPayloadType().equals(eventType))
              .toList();
          return currentActualEvents;
        },
        1
    );
    Assertions.assertTrue(actualEvents.size() > 0, "No events of that type found.");
    return (TrackedEventMessage<T>) actualEvents.get(actualEvents.size() - 1);
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

  private void approveElementsForRemoval(List<GraphElementForRemoval> removals) {
    this.thenObjectApproved(
        removals.stream()
            .sorted(Comparator.comparing(GraphElementForRemoval::getGraphElementId))
            .collect(Collectors.toList()),
        new ObjectToJSonStringOptions(
            ObjectToJSonStringOptions.RenderFeature.SORT_FIELDS,
            ObjectToJSonStringOptions.RenderFeature.HIDE_IDS
        )
    );
  }

  private void thenSavedEventsCanBeSerialized() {
    var actualEvents = this.getActualEvents(this.getActualEventStream());
    actualEvents.forEach(this::thenEventCanBeSerialized);
  }

  private void thenEventCanBeSerialized(Object recordedEvent) {
    var serializedEvent = this.serializer.serialize(
        recordedEvent,
        String.class
    );
    var deserializedObject = this.serializer.deserialize(serializedEvent);
    assertTrue(
        deserializedObject instanceof Event,
        String.format(
            "\n%s\ndoes not implement:\n%s.",
            deserializedObject.getClass(),
            Event.class
        )
    );
    var deserializedEvent = (Event) deserializedObject;
    Assertions.assertEquals(recordedEvent.getClass(), deserializedEvent.getClass());
    //TODO: Make working equals
  }

  private void assertEventTypeListsAreSame(
      List<Class<?>> expectedEventTypes,
      List<Class<?>> actualEventTypes
  ) {
    var actualEventTypesIterator = actualEventTypes.iterator();
    expectedEventTypes.forEach(expectedEventType ->
        Assertions.assertEquals(
            expectedEventType,
            actualEventTypesIterator.next(),
            this.getErrorMessage(expectedEventTypes, actualEventTypes)
        )
    );
  }

  private void assertDynamicEventsHaveCorrectNames(
      List<String> expectedEventNames,
      List<DynamicGraphUpdatedEvent> actualEvents
  ) {
    var actualEventsIterator = actualEvents.iterator();
    expectedEventNames.forEach(expectedEventName ->
        Assertions.assertEquals(
            expectedEventName,
            actualEventsIterator.next().getEventName(),
            this.getDynamicEventsErrorMessage(expectedEventNames, actualEvents)
        )
    );
  }

  @NotNull
  private String getDynamicEventsErrorMessage(
      List<String> expectedEventNames,
      List<DynamicGraphUpdatedEvent> actualEvents
  ) {
    return "Expected event names dont match.\nExpected: %s\nActual: %s".formatted(
        expectedEventNames,
        actualEvents.stream()
            .map(DynamicGraphUpdatedEvent::getEventName)
            .toList()
    );
  }

  private List<Class<?>> getActualEventTypes(TrackingEventStream actualEventStream) {
    var actualEventTypes = new ArrayList<Class<?>>();
    while (actualEventStream.hasNextAvailable()) {
      actualEventTypes.add(getActualEventType(actualEventStream));
    }
    actualEventStream.close();
    return actualEventTypes;
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

  private Class<?> getActualEventType(TrackingEventStream actualEventStream) {
    try {
      return actualEventStream.nextAvailable().getPayloadType();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  protected String getErrorMessage(
      List<Class<?>> expectedEventTypes,
      List<Class<?>> actualEventTypes
  ) {
    var title = LineFormatter.createLine("Actual event types differ from expected event types.");
    var expectedTitle = LineFormatter.createLine("Expected:");
    var expectedString = LineFormatter.createLines(
        expectedEventTypes.stream().map(Class::toString)
    );
    var actualTitle = LineFormatter.createLine("Actual:");
    var actualString = LineFormatter.createLines(
        actualEventTypes.stream().map(Class::toString)
    );
    return title + expectedTitle + expectedString + actualTitle + actualString;
  }

  private String getErrorMessage(Class<?> expectedEventType, List<Class<?>> actualEventTypes) {
    var title =
        LineFormatter.createLine("Actual event types does not contain expected event type.");
    var expectedTitle = LineFormatter.createLine("Expected:");
    var expectedString = LineFormatter.createLine(expectedEventType.toString());
    var actualTitle = LineFormatter.createLine("Actual:");
    var actualString = LineFormatter.createLines(
        actualEventTypes.stream().map(Class::toString)
    );
    return title + expectedTitle + expectedString + actualTitle + actualString;
  }

  private TrackingEventStream getActualEventStream() {
    var token = eventStore.createTokenAt(this.testStartedAt);
    return eventStore.openStream(token);
  }
}
