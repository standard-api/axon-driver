package ai.stapi.axonsystem.configuration;

import ai.stapi.axonsystem.commandvalidation.CommandValidatorDispatchInterceptor;
import ai.stapi.axonsystem.configuration.implementations.CommandDispatchedAtInterceptor;
import ai.stapi.axonsystem.configuration.implementations.CustomFailureLoggingCallback;
import ai.stapi.graphsystem.commandvalidation.model.CommandValidator;
import java.util.List;
import java.util.logging.Logger;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.DuplicateCommandHandlerResolver;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.MessageOriginProvider;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@AutoConfiguration
public class CommandGatewayConfiguration {

  @Bean
  @ConditionalOnMissingBean(CommandGateway.class)
  public DefaultCommandGateway configuredCommandGateway(
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
  @ConditionalOnMissingBean
  public CommandBus configuredCommandBus(
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
  
  @Bean
  public CommandDispatchedAtInterceptor commandDispatchedAtInterceptor() {
    return new CommandDispatchedAtInterceptor();
  }
  
  @Bean
  @Profile("dev")
  public CommandValidatorDispatchInterceptor commandValidatorDispatchInterceptor(
      CommandValidator commandValidator
  ) {
    return new CommandValidatorDispatchInterceptor(commandValidator);
  }

  @Bean
  public CorrelationDataProvider messageCorrelationProvider() {
    return new MessageOriginProvider();
  }
}
