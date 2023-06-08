package ai.stapi.axonsystem.commandpersisting;

import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.InterceptorChain;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.unitofwork.UnitOfWork;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class PersistCommandInterceptor implements MessageHandlerInterceptor<CommandMessage<?>> {

  private final CommandMessageStore commandMessageStore;

  public PersistCommandInterceptor(CommandMessageStore commandMessageStore) {
    this.commandMessageStore = commandMessageStore;
  }

  @Override
  public Object handle(
      @NotNull UnitOfWork<? extends CommandMessage<?>> unitOfWork,
      @NotNull InterceptorChain interceptorChain
  ) throws Exception {
    unitOfWork.afterCommit(unitOfWorkAfterCommit -> {
      var message = unitOfWorkAfterCommit.getMessage();
      this.commandMessageStore.storeCommand(message);
    });
    return interceptorChain.proceed();
  }
}
