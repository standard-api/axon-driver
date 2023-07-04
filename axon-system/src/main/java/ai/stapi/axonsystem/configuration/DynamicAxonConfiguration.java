package ai.stapi.axonsystem.configuration;

import ai.stapi.axonsystem.dynamic.aggregate.AggregateDefinitionDACProvider;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateConfigurationsProvider;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateConfigurer;
import ai.stapi.axonsystem.dynamic.aggregate.RuntimeDynamicAggregateConfigurer;
import ai.stapi.axonsystem.dynamic.command.CustomCommandTargetResolver;
import ai.stapi.axonsystem.dynamic.command.DynamicCommandDispatchInterceptor;
import ai.stapi.axonsystem.dynamic.messagehandler.DynamicMessageHandlerLookup;
import ai.stapi.axonsystem.dynamic.messagehandler.DynamicMessageHandlingMemberDefinition;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.tracing.SpanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Role;

@AutoConfiguration
public class DynamicAxonConfiguration {
  
  @Bean
  public DynamicAggregateConfigurer dynamicAggregateConfigurer(
      DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider,
      RuntimeDynamicAggregateConfigurer runtimeDynamicAggregateConfigurer
  ) {
    return new DynamicAggregateConfigurer(
        dynamicAggregateConfigurationsProvider,
        runtimeDynamicAggregateConfigurer
    );
  }
  
  @Bean
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public DynamicMessageHandlerLookup dynamicMessageHandlerLookup() {
    return new DynamicMessageHandlerLookup();
  }
  
  @Bean
  public DynamicMessageHandlingMemberDefinition dynamicMessageHandlingMemberDefinition() {
    return new DynamicMessageHandlingMemberDefinition();
  }
  
  @Bean
  public DynamicCommandDispatchInterceptor dynamicCommandDispatchInterceptor() {
    return new DynamicCommandDispatchInterceptor();
  }
  
  @Bean
  public CustomCommandTargetResolver customCommandTargetResolver() {
    return new CustomCommandTargetResolver();
  }
  
  @Bean
  @ConditionalOnMissingBean(DynamicAggregateConfigurationsProvider.class)
  public AggregateDefinitionDACProvider aggregateDefinitionDACProvider(
      AggregateDefinitionProvider aggregateDefinitionProvider,
      DynamicCommandProcessor dynamicCommandProcessor,
      @Lazy EventStore eventStore,
      SpanFactory spanFactory,
      @Lazy Configuration configuration,
      ParameterResolverFactory parameterResolverFactory,
      CommandTargetResolver commandTargetResolver
  ) {
    return new AggregateDefinitionDACProvider(
        aggregateDefinitionProvider,
        dynamicCommandProcessor,
        eventStore,
        spanFactory,
        configuration,
        parameterResolverFactory,
        commandTargetResolver
    );
  }
  
  @Bean
  public RuntimeDynamicAggregateConfigurer runtimeDynamicAggregateConfigurer(
      DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider,
      @Lazy Configurer configurer
  ) {
    return new RuntimeDynamicAggregateConfigurer(
        dynamicAggregateConfigurationsProvider,
        configurer
    );
  }
}
