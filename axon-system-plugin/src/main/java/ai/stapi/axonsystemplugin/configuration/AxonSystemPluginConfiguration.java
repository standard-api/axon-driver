package ai.stapi.axonsystemplugin.configuration;

import ai.stapi.axonsystem.commandpersisting.CommandMessageStore;
import ai.stapi.axonsystemplugin.DefaultGraphProjection;
import ai.stapi.axonsystemplugin.commandpersisting.CommitCommandFixturesHandler;
import ai.stapi.axonsystemplugin.commandpersisting.CommitCommandFixturesLineRunner;
import ai.stapi.axonsystemplugin.commandpersisting.WipePersistedCommandAfterCommitPolicy;
import ai.stapi.axonsystemplugin.fixtures.FixtureCommandsApplier;
import ai.stapi.axonsystemplugin.fixtures.GenerateFixturesCommandLineRunner;
import ai.stapi.axonsystemplugin.structuredefinition.configure.ConfigureStructureDefinitionHandler;
import ai.stapi.graph.EdgeRepository;
import ai.stapi.graph.NodeRepository;
import ai.stapi.graphoperations.synchronization.GraphSynchronizer;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

@AutoConfiguration
@ComponentScan("ai.stapi.axonsystemplugin.fixtures")
@ComponentScan("ai.stapi.axonsystemplugin.structuredefinition")
public class AxonSystemPluginConfiguration {
  
  @Bean
  public DefaultGraphProjection defaultGraphProjection(
      NodeRepository nodeRepository,
      EdgeRepository edgeRepository,
      GraphSynchronizer graphSynchronizer
  ) {
    return new DefaultGraphProjection(nodeRepository, edgeRepository, graphSynchronizer);
  }
  
  @Bean
  public ConfigureStructureDefinitionHandler configureStructureDefinitionHandler(
      EventGateway eventGateway,
      StructureSchemaProvider structureSchemaProvider,
      StructureSchemaFinder structureSchemaFinder
  ) {
    return new ConfigureStructureDefinitionHandler(
        eventGateway,
        structureSchemaProvider,
        structureSchemaFinder
    );
  }
  
  @Bean
  public FixtureCommandsApplier fixtureCommandsApplier(CommandGateway commandGateway) {
    return new FixtureCommandsApplier(commandGateway);
  }

  @Bean
  public CommitCommandFixturesHandler commitCommandFixturesHandler(
      CommandMessageStore commandMessageStore,
      EventGateway eventGateway,
      ObjectMapper objectMapper
  ) {
    return new CommitCommandFixturesHandler(
        commandMessageStore,
        eventGateway,
        objectMapper
    );
  }
  
  @Bean
  public WipePersistedCommandAfterCommitPolicy wipePersistedCommandAfterCommitPolicy(
      CommandMessageStore commandMessageStore
  ) {
    return new WipePersistedCommandAfterCommitPolicy(commandMessageStore);
  }
  
  @Bean
  @Profile("generate-fixtures")
  public GenerateFixturesCommandLineRunner generateFixturesCommandLineRunner(
      CommandGateway commandGateway,
      DefaultGraphProjection graphProjection,
      ApplicationContext applicationContext
  ) {
    return new GenerateFixturesCommandLineRunner(
        commandGateway,
        graphProjection,
        applicationContext
    );
  }
  
  @Bean
  @Profile("commit-command-fixtures")
  public CommitCommandFixturesLineRunner commitCommandFixturesLineRunner(
      CommandGateway commandGateway,
      ApplicationContext applicationContext
  ) {
    return new CommitCommandFixturesLineRunner(commandGateway, applicationContext);
  }
}
