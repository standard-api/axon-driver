package ai.stapi.test.fixtureQueryTest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ai.stapi.graphsystem.messaging.query.Query;
import ai.stapi.graphsystem.messaging.query.Response;
import ai.stapi.graphsystem.systemfixtures.model.SystemModelDefinitionsLoader;
import ai.stapi.test.base.AbstractAxonTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.thoughtworks.xstream.XStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(SystemModelDefinitionsLoader.SCOPE)
public abstract class AbstractFixtureQueryTestCase extends AbstractAxonTestCase {

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
      Thread.currentThread().interrupt();
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
      Thread.currentThread().interrupt();
    }
    Assertions.assertNotNull(actualResponse);
    this.thenQueryCanBeSerialized(query);
    if (actualResponse != null) {
      this.thenResponseCanBeSerializedWithJsonSerializer(actualResponse);
    }

    return actualResponse;
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
      Thread.currentThread().interrupt();
    }
    Assertions.assertNotNull(actualResponse);
    this.thenQueryCanBeSerialized(query);
    if (actualResponse != null) {
      this.thenResponseCanBeSerializedWithJsonSerializer(actualResponse);
    }
    return actualResponse;
  }

  protected <T> List<T> whenQueryReturningListIsDispatched(
      Query query,
      Class<T> expectedResponseType
  ) {
    var actualResponse = this.makeQueryReturningList(query, expectedResponseType);
    if (actualResponse == null) {
      Assertions.fail("Response is null");
      throw new RuntimeException("Response is null");
    }
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

  private <T> List<T> makeQueryReturningList(Query query, Class<T> expectedResponseType) {
    var completableFuture = queryGateway.query(
        query,
        ResponseTypes.multipleInstancesOf(expectedResponseType)
    );
    List<T> actualResponse = null;
    try {
      actualResponse = completableFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
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
