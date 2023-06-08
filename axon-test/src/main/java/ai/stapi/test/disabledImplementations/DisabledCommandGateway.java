package ai.stapi.test.disabledImplementations;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.common.Registration;
import org.axonframework.messaging.MessageDispatchInterceptor;

public class DisabledCommandGateway implements CommandGateway {

  @Override
  public <C, R> void send(C c, CommandCallback<? super C, ? super R> commandCallback) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowSendingCommands();
  }

  @Override
  public <R> R sendAndWait(Object o) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowSendingCommands();
  }

  @Override
  public <R> R sendAndWait(Object o, long l, TimeUnit timeUnit) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowSendingCommands();
  }

  @Override
  public <R> CompletableFuture<R> send(Object o) {
    throw InvalidTestOperation.becauseTestCaseDoesntAllowSendingCommands();
  }

  @Override
  public Registration registerDispatchInterceptor(
      MessageDispatchInterceptor<? super CommandMessage<?>> messageDispatchInterceptor) {
    return () -> false;
  }
}
