package ai.stapi.test.domain;

import ai.stapi.test.disabledImplementations.DisabledQueryGateway;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;


@Profile("test")
public class DomainTestConfig {

  @Bean
  @Primary
  public static QueryGateway createQueryGateway() {
    return new DisabledQueryGateway();
  }

  @Bean
  public static EventStore createEventStore(
      EventStorageEngine eventStorageEngine
  ) {
    return new EmbeddedEventStore.Builder().storageEngine(eventStorageEngine).build();
  }

  @Bean
  public static EventStorageEngine createEventStorageEngine() {
    return new TestInMemoryEventStorageEngine();
  }

  @Bean
  @Primary
  public static TokenStore createTokenStore() {
    return new InMemoryTokenStore();
  }
  

  @Bean
  public ConfigurerModule processorDefaultConfigurerModule() {
    return configurer -> configurer.eventProcessing(
        eventProcessingConfigurer -> eventProcessingConfigurer
            .registerDefaultListenerInvocationErrorHandler(
                configuration -> PropagatingErrorHandler.INSTANCE)
            .usingSubscribingEventProcessors()
    );
  }
}
