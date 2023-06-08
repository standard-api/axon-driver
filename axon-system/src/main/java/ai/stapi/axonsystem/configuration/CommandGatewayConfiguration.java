package ai.stapi.axonsystem.configuration;

import java.util.List;
import java.util.logging.Logger;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.DuplicateCommandHandlerResolver;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommandGatewayConfiguration {

  @Bean
  public DefaultCommandGateway defaultCommandGateway(
      @Autowired CommandBus commandBus,
      @Autowired
      List<MessageDispatchInterceptor<? super CommandMessage<?>>> messageDispatchInterceptors
  ) {
    var failureCommandCallback = new CustomFailureLoggingCallback<>(
        Logger.getLogger(DefaultCommandGateway.class.getSimpleName())
    );
    return DefaultCommandGateway.builder()
        .commandBus(commandBus)
        .commandCallback(failureCommandCallback)
        .dispatchInterceptors(messageDispatchInterceptors)
        .build();
  }

  @Bean
  public CommandBus commandBus(
      @Autowired TransactionManager txManager,
      @Autowired org.axonframework.config.Configuration axonConfiguration,
      @Autowired DuplicateCommandHandlerResolver duplicateCommandHandlerResolver,
      @Autowired
      List<MessageHandlerInterceptor<? super CommandMessage<?>>> messageHandlerInterceptors
  ) {
    var commandBus = SimpleCommandBus.builder()
        .transactionManager(txManager)
        .duplicateCommandHandlerResolver(duplicateCommandHandlerResolver)
        .spanFactory(axonConfiguration.spanFactory())
        .messageMonitor(axonConfiguration.messageMonitor(CommandBus.class, "commandBus"))
        .build();

    commandBus.registerHandlerInterceptor(
        new CorrelationDataInterceptor<>(axonConfiguration.correlationDataProviders())
    );

    messageHandlerInterceptors.forEach(commandBus::registerHandlerInterceptor);

    return commandBus;
  }
}
