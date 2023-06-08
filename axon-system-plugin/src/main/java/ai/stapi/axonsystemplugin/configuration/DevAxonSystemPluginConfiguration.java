package ai.stapi.axonsystemplugin.configuration;

import ai.stapi.axonsystemplugin.aggregatedefinition.CreateAggregateDefinitionPolicy;
import ai.stapi.axonsystemplugin.aggregatedefinition.createCRUDCommandHandlers.CreateAddItemCommandHandlerPolicy;
import ai.stapi.axonsystemplugin.aggregatedefinition.createCRUDCommandHandlers.CreateCreationalCommandHandlerPolicy;
import ai.stapi.axonsystemplugin.structuredefinition.configure.ConfigureImportedStructureDefinitionPolicy;
import ai.stapi.graphoperations.graphDeserializers.ogmDeserializer.GenericGraphToObjectDeserializer;
import ai.stapi.graphsystem.aggregatedefinition.model.ResourceAggregateDefinitionMapper;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.CreatedOperationEventFactoriesMapper;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.ItemAddedOperationEventFactoriesMapper;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.AddItemOnResourceOperationsMapper;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.CreationalResourceOperationMapper;
import ai.stapi.schema.structureSchemaMapper.StructureDefinitionToSSMapper;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@AutoConfiguration
@Profile("dev")
public class DevAxonSystemPluginConfiguration {

  @Bean
  public ConfigureImportedStructureDefinitionPolicy configureImportedStructureDefinitionPolicy(
      GenericGraphToObjectDeserializer genericGraphToObjectDeserializer,
      CommandGateway commandGateway
  ) {
    return new ConfigureImportedStructureDefinitionPolicy(
        genericGraphToObjectDeserializer,
        commandGateway
    );
  }

  @Bean
  public CreateAggregateDefinitionPolicy createAggregateDefinitionPolicy(
      CommandGateway commandGateway,
      StructureSchemaFinder structureSchemaFinder,
      ResourceAggregateDefinitionMapper aggregateDefinitionMapper,
      ObjectMapper objectMapper
  ) {
    return new CreateAggregateDefinitionPolicy(
        commandGateway,
        structureSchemaFinder,
        aggregateDefinitionMapper,
        objectMapper
    );
  }

  @Bean
  public CreateCreationalCommandHandlerPolicy createCreationalCommandHandlerPolicy(
      CommandGateway commandGateway,
      StructureSchemaFinder structureSchemaFinder,
      ObjectMapper objectMapper,
      StructureDefinitionToSSMapper structureDefinitionToSSMapper,
      CreationalResourceOperationMapper creationalResourceOperationMapper,
      CreatedOperationEventFactoriesMapper createdOperationEventDefinitionMapper
  ) {
    return new CreateCreationalCommandHandlerPolicy(
        commandGateway,
        structureSchemaFinder,
        objectMapper,
        structureDefinitionToSSMapper,
        creationalResourceOperationMapper,
        createdOperationEventDefinitionMapper
    );
  }

  @Bean
  public CreateAddItemCommandHandlerPolicy createAddItemCommandHandlerPolicy(
      CommandGateway commandGateway,
      StructureSchemaFinder structureSchemaFinder,
      ObjectMapper objectMapper,
      StructureDefinitionToSSMapper structureDefinitionToSSMapper,
      AddItemOnResourceOperationsMapper addItemOnResourceOperationsMapper,
      ItemAddedOperationEventFactoriesMapper itemAddedOperationEventMessageMapper
  ) {
    return new CreateAddItemCommandHandlerPolicy(
        commandGateway,
        structureSchemaFinder,
        objectMapper,
        structureDefinitionToSSMapper,
        addItemOnResourceOperationsMapper,
        itemAddedOperationEventMessageMapper
    );
  }
}
