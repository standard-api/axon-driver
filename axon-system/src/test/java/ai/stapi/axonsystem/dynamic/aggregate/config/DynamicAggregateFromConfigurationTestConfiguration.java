package ai.stapi.axonsystem.dynamic.aggregate.config;

import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateConfigurationsProvider;
import ai.stapi.axonsystem.dynamic.aggregate.testImplementations.TestDynamicAggregateConfigurationProvider;
import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import org.axonframework.config.Configuration;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.tracing.SpanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;


public class DynamicAggregateFromConfigurationTestConfiguration {

  @Bean
  @Primary
  public DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider(
      DynamicCommandProcessor dynamicCommandProcessor,
      @Lazy EventStore eventStore,
      SpanFactory spanFactory,
      @Lazy Configuration configuration,
      ParameterResolverFactory parameterResolverFactory,
      CommandTargetResolver commandTargetResolver
  ) {
    return new TestDynamicAggregateConfigurationProvider(
        dynamicCommandProcessor,
        eventStore,
        spanFactory,
        configuration,
        parameterResolverFactory,
        commandTargetResolver
    );
  }
}
