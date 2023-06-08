package ai.stapi.test.fixtureQueryTest;

import ai.stapi.graphoperations.synchronization.DisabledGraphSynchronizer;
import ai.stapi.graphoperations.synchronization.GraphSynchronizer;
import ai.stapi.graphsystem.aggregatedefinition.infrastructure.AdHocAggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.infrastructure.DatabaseAggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.infrastructure.AdHocOperationDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.infrastructure.DatabaseOperationDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import ai.stapi.graphsystem.structuredefinition.loader.CombinedStructureDefinitionLoader;
import ai.stapi.graphsystem.structuredefinition.loader.SystemAdHocStructureDefinitionLoader;
import ai.stapi.schema.structuredefinition.loader.StructureDefinitionLoader;
import ai.stapi.test.disabledImplementations.DisabledCommandGateway;
import ai.stapi.test.disabledImplementations.DisabledEventGateway;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.inmemory.InMemoryTokenStore;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.SimpleQueryBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Profile("test")
public class FixtureQueryTestCaseConfig {

  @Bean
  public static StructureDefinitionLoader structureDefinitionLoader(
      CombinedStructureDefinitionLoader combinedStructureDefinitionLoader,
      SystemAdHocStructureDefinitionLoader adHocStructureDefinitionLoader
  ) {
    return combinedStructureDefinitionLoader;
  }

  @Bean
  public static OperationDefinitionProvider operationDefinitionProvider(
      @Autowired DatabaseOperationDefinitionProvider databaseOperationDefinitionProvider,
      AdHocOperationDefinitionProvider adHocOperationDefinitionProvider
  ) {
    return databaseOperationDefinitionProvider;
  }

  @Bean
  public static AggregateDefinitionProvider aggregateDefinitionProvider(
      @Autowired DatabaseAggregateDefinitionProvider databaseAggregateDefinitionProvider,
      AdHocAggregateDefinitionProvider adHocAggregateDefinitionProvider
  ) {
    return databaseAggregateDefinitionProvider;
  }

  @Bean
  @Primary
  public static GraphSynchronizer createDisabledSynchronizer() {
    return new DisabledGraphSynchronizer();
  }

  @Bean
  @Primary
  public static QueryBus createQueryBus() {
    return new SimpleQueryBus.Builder().build();
  }

  @Bean
  public static EventStore createEventStore() {
    return new EmbeddedEventStore.Builder().storageEngine(new InMemoryEventStorageEngine()).build();
  }

  @Bean
  @Primary
  public static EventGateway createEventGateway() {
    return new DisabledEventGateway();
  }

  @Bean
  @Primary
  public static CommandGateway createCommandBus() {

    return new DisabledCommandGateway();
  }

  @Bean
  @Primary
  public static TokenStore createTokenStore() {
    return new InMemoryTokenStore();
  }

}
