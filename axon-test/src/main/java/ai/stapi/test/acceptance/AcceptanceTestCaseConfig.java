package ai.stapi.test.acceptance;

import ai.stapi.graphsystem.structuredefinition.loader.DatabaseStructureDefinitionLoader;
import ai.stapi.schema.structuredefinition.loader.StructureDefinitionLoader;
import org.axonframework.config.ConfigurerModule;
import org.axonframework.eventhandling.PropagatingErrorHandler;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


public class AcceptanceTestCaseConfig {

  @Bean
  @Primary
  public static StructureDefinitionLoader structureDefinitionLoader(
      DatabaseStructureDefinitionLoader dbLoader) {
    return dbLoader;
  }
  
  @Bean
  public static EventStore createEventStore() {
    return new EmbeddedEventStore.Builder().storageEngine(new InMemoryEventStorageEngine()).build();
  }

  @Bean
  @Primary
  public static TokenStore createTokenStore() {
    return new InMemoryTokenStore();
  }

  @Bean
  @Primary
  public static QueryBus createQueryBus() {
    return new SimpleQueryBus.Builder().build();
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
