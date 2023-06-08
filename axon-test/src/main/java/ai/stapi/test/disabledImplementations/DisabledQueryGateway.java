package ai.stapi.test.disabledImplementations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.axonframework.common.Registration;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.responsetypes.ResponseType;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryMessage;
import org.axonframework.queryhandling.SubscriptionQueryBackpressure;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

public class DisabledQueryGateway implements QueryGateway {

  @Override
  public <R, Q> CompletableFuture<R> query(
      @NotNull String queryName,
      @NotNull Q query,
      @NotNull ResponseType<R> responseType
  ) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowQuerying();
  }

  @Override
  public <R, Q> Publisher<R> streamingQuery(
      String queryName,
      Q query,
      Class<R> responseType
  ) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowQuerying();
  }

  @Override
  public <R, Q> Stream<R> scatterGather(
      @NotNull String queryName,
      @NotNull Q query,
      @NotNull ResponseType<R> responseType,
      long timeout,
      @NotNull TimeUnit timeUnit
  ) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowQuerying();
  }

  @Override
  public <Q, I, U> SubscriptionQueryResult<I, U> subscriptionQuery(
      @NotNull String queryName,
      @NotNull Q query,
      @NotNull ResponseType<I> initialResponseType,
      @NotNull ResponseType<U> updateResponseType,
      @Nullable SubscriptionQueryBackpressure backpressure,
      int updateBufferSize
  ) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowQuerying();
  }

  @Override
  public <Q, I, U> SubscriptionQueryResult<I, U> subscriptionQuery(
      @NotNull String queryName,
      @NotNull Q query,
      @NotNull ResponseType<I> initialResponseType,
      @NotNull ResponseType<U> updateResponseType,
      int updateBufferSize
  ) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowQuerying();
  }

  @Override
  public Registration registerDispatchInterceptor(
      @NotNull MessageDispatchInterceptor<? super QueryMessage<?, ?>> dispatchInterceptor
  ) {
    return () -> false;
  }
}
