package ai.stapi.test.fixtureQueryTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.stapi.graph.attribute.AbstractAttributeContainer;
import ai.stapi.graph.traversableGraphElements.TraversableEdge;
import ai.stapi.graph.traversableGraphElements.TraversableGraphElement;
import ai.stapi.graph.traversableGraphElements.TraversableNode;
import ai.stapi.graphsystem.messaging.query.Query;
import ai.stapi.graphsystem.messaging.query.Response;
import ai.stapi.graphsystem.systemfixtures.model.SystemModelDefinitionsLoader;
import ai.stapi.test.schemaintegration.AbstractSchemaIntegrationTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import ai.stapi.utils.LineFormatter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.XStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.approvaltests.Approvals;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventStream;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(SystemModelDefinitionsLoader.SCOPE)
public abstract class AbstractFixtureQueryTestCase extends AbstractSchemaIntegrationTestCase {

  @Autowired
  private QueryGateway queryGateway;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private XStream xStream;
  

  @Deprecated
  protected void thenQueryWillReturnResponseOfType(
      Query query,
      Class<? extends Object> expectedResponseType
  ) {
    var completableFuture = queryGateway.query(
        query,
        ResponseTypes.instanceOf(expectedResponseType)
    );
    Object actualEntity = null;
    try {
      actualEntity = completableFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      e.printStackTrace();
    }
    Assertions.assertNotNull(actualEntity);
  }

  protected <T> T whenQueryIsDispatched(
      Query query,
      Class<T> expectedResponseType
  ) {
    var completableFuture = queryGateway.query(
        query,
        ResponseTypes.instanceOf(expectedResponseType)
    );
    T actualResponse = null;
    try {
      actualResponse = completableFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    Assertions.assertNotNull(actualResponse);
    this.thenQueryCanBeSerialized(query);
    this.thenResponseCanBeSerializedWithJsonSerializer(actualResponse);

    return actualResponse;
  }


  private void assertEventTypeListsAreSame(List<Class<?>> expectedEventTypes,
      List<Class<?>> actualEventTypes) {
    var actualEventTypesIterator = actualEventTypes.iterator();
    expectedEventTypes.forEach(expectedEventType ->
        Assertions.assertEquals(
            expectedEventType,
            actualEventTypesIterator.next(),
            this.getErrorMessage(expectedEventTypes, actualEventTypes)
        )
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

  private List<Object> getActualEvents(TrackingEventStream actualEventStream) {
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
      TrackingEventStream actualEventStream) {
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

  private String getErrorMessage(List<Class<?>> expectedEventTypes,
      List<Class<?>> actualEventTypes) {
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

  @Nullable
  protected <T> T whenNullableQueryIsDispatched(
      Query query,
      Class<T> expectedResponseType
  ) {
    var completableFuture = queryGateway.query(
        query,
        ResponseTypes.instanceOf(expectedResponseType)
    );
    T actualResponse = null;
    try {
      actualResponse = completableFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    this.thenQueryCanBeSerialized(query);
    if (actualResponse != null) {
      this.thenResponseCanBeSerializedWithJsonSerializer(actualResponse);
    }
    return actualResponse;
  }

  protected void thenGraphElementsApprovedSorted(List<TraversableGraphElement> graphElements) {
    Assertions.assertNotNull(graphElements);
    if (graphElements.size() == 0) {
      Approvals.verify(new ArrayList<>());
      return;
    }
    if (graphElements.get(0) instanceof TraversableNode) {
      var nodes = graphElements.stream()
          .map(node -> (TraversableNode) node)
          .sorted(Comparator.comparingInt(AbstractAttributeContainer::getIdlessHashCode))
          .toList();
      this.thenNodesApproved(nodes);
    }
    if (graphElements.get(0) instanceof TraversableEdge) {
      var edges = graphElements.stream()
          .map(edge -> (TraversableEdge) edge)
          .sorted(Comparator.comparingInt(AbstractAttributeContainer::getIdlessHashCode))
          .toList();
      this.thenEdgesApproved(edges);
    }
  }


  protected void thenNodeApproved(TraversableNode node) {
    Assertions.assertNotNull(node);
    var renderedNode = idLessTextNodeRenderer.render(node).toPrintableString();
    Approvals.verify(renderedNode);
  }

  protected void thenNodesApproved(List<TraversableNode> nodes) {
    Assertions.assertNotNull(nodes);
    if (nodes.size() == 0) {
      Approvals.verify(new ArrayList<>());
      return;
    }
    var renderedNodes = nodes.stream()
        .map(node -> idLessTextNodeRenderer.render(node).toPrintableString());
    Approvals.verify(LineFormatter.createLines(renderedNodes));
  }

  protected void thenEdgeApproved(TraversableEdge edge) {
    var renderEdge =
        idLessTextEdgeRenderer.render(edge, this.getRenderOptions()).toPrintableString();
    Approvals.verify(renderEdge);
  }

  protected void thenEdgesApproved(List<TraversableEdge> edges) {
    Assertions.assertNotNull(edges);
    if (edges.size() == 0) {
      Approvals.verify(new ArrayList<>());
      return;
    }
    var renderedEdges = edges.stream()
        .map(edge -> idLessTextEdgeRenderer.render(edge).toPrintableString())
        .sorted();
    Approvals.verify(LineFormatter.createLines(renderedEdges));
  }

  protected <T> List<T> whenQueryReturningListIsDispatched(
      Query query,
      Class<T> expectedResponseType
  ) {
    var actualResponse = makeQueryReturningList(query, expectedResponseType);
    this.thenQueryCanBeSerialized(query);
    actualResponse.forEach(this::thenResponseCanBeSerializedWithJsonSerializer);
    return actualResponse;

  }

  protected <T> List<T> givenQueryReturningListIsDispatched(
      Query query,
      Class<T> expectedResponseType
  ) {
    return makeQueryReturningList(query, expectedResponseType);
  }

  @NotNull
  private <T> List<T> makeQueryReturningList(Query query, Class<T> expectedResponseType) {
    var completableFuture = queryGateway.query(
        query,
        ResponseTypes.multipleInstancesOf(expectedResponseType)
    );
    List<T> actualResponse = null;
    try {
      actualResponse = completableFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
    Assertions.assertNotNull(actualResponse);
    return actualResponse;
  }

  private void thenQueryCanBeSerialized(Object queryToSerialize) {
    this.thenQueryCanBeSerializedWithXStream(queryToSerialize);
    this.thenQueryCanBeSerializedWithJsonSerializer(queryToSerialize);
  }

  private void thenQueryCanBeSerializedWithXStream(Object queryToSerialize) {
    var serializer = XStreamSerializer.builder().xStream(this.xStream).build();
    var serializedQuery = serializer.serialize(
        queryToSerialize,
        String.class
    );
    var deserializedObject = serializer.deserialize(serializedQuery);
    assertTrue(
        deserializedObject instanceof Query,
        deserializedObject.getClass() + " does not implement query interface."
    );
    var deserializedQuery = (Query) deserializedObject;
    Assertions.assertEquals(queryToSerialize.getClass(), deserializedQuery.getClass());
    //TODO: Make working equals
  }

  private void thenQueryCanBeSerializedWithJsonSerializer(Object queryToSerialize) {
    var serializer = this.objectMapper;
    serializer.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    String serializedQuery;
    try {
      serializedQuery = serializer.writeValueAsString(queryToSerialize);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    Object deserializedObject;
    try {
      deserializedObject = serializer.readValue(serializedQuery, queryToSerialize.getClass());
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    assertTrue(
        deserializedObject instanceof Query,
        deserializedObject.getClass() + " does not implement query interface."
    );
    var deserializedQuery = (Query) deserializedObject;
    Assertions.assertEquals(queryToSerialize.getClass(), deserializedQuery.getClass());
    //TODO: Make working equals
  }

  private void thenResponseCanBeSerializedWithJsonSerializer(Object responseToSerialize) {
    var serializer = this.objectMapper;
    serializer.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    try {
      serializer.writeValueAsString(responseToSerialize);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    assertTrue(
        responseToSerialize instanceof Response
            || responseToSerialize instanceof LinkedHashMap<?, ?>,
        responseToSerialize.getClass() + " does not implement the \"Response\" interface. \n " +
            "Object returned from query must implement \"" + Response.class + "\" interface."
    );
  }

}
