package ai.stapi.test.disabledImplementations;

import java.util.List;
import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.jetbrains.annotations.NotNull;

public class DisabledEventGateway implements EventGateway {

  @Override
  public void publish(@NotNull List<?> events) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowPublishingEvents();
  }

  @Override
  public Registration registerDispatchInterceptor(
      @NotNull MessageDispatchInterceptor<? super EventMessage<?>> dispatchInterceptor
  ) {
    return () -> false;
  }
}
