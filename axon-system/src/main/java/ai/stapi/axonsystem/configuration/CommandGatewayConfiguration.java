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
import org.axonframework.config.Configuration;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.MessageHandlerInterceptor;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@AutoConfiguration
@AutoConfigureAfter({AxonAutoConfiguration.class})
public class CommandGatewayConfiguration {

  @Autowired
  private ObjectProvider<TransactionManager> transactionManagerProvider;

  @Bean
  @Primary
  public CommandGateway configuredCommandGateway(
      CommandBus commandBus,
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
  public SimpleCommandBus configuredCommandBus(
      Configuration axonConfiguration,
      DuplicateCommandHandlerResolver duplicateCommandHandlerResolver,
      List<MessageHandlerInterceptor<? super CommandMessage<?>>> messageHandlerInterceptors
  ) {
    SimpleCommandBus.Builder commandBusBuilder = SimpleCommandBus.builder();

    TransactionManager transactionManager = transactionManagerProvider.getIfAvailable();
    if (transactionManager != null) {
      commandBusBuilder.transactionManager(transactionManager);
    }

    SimpleCommandBus commandBus = commandBusBuilder
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

}
