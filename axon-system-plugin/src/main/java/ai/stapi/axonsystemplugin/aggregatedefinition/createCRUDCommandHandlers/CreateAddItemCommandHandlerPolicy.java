package ai.stapi.axonsystemplugin.aggregatedefinition.createCRUDCommandHandlers;

import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.ItemAddedOperationEventFactoriesMapper;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.OperationEventFactoriesMapper;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.AddItemOnResourceOperationsMapper;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.ResourceOperationsMapper;
import ai.stapi.schema.structureSchemaMapper.StructureDefinitionToSSMapper;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;

public class CreateAddItemCommandHandlerPolicy extends AbstractCreateCRUDCommandHandlerPolicy {

  private final AddItemOnResourceOperationsMapper addItemOnResourceOperationsMapper;
  private final ItemAddedOperationEventFactoriesMapper itemAddedOperationEventMessageMapper;

  public CreateAddItemCommandHandlerPolicy(
      CommandGateway commandGateway,
      StructureSchemaFinder structureSchemaFinder,
      ObjectMapper objectMapper,
      StructureDefinitionToSSMapper structureDefinitionToSSMapper,
      AddItemOnResourceOperationsMapper addItemOnResourceOperationsMapper,
      ItemAddedOperationEventFactoriesMapper itemAddedOperationEventMessageMapper
  ) {
    super(commandGateway, structureSchemaFinder, objectMapper, structureDefinitionToSSMapper);
    this.addItemOnResourceOperationsMapper = addItemOnResourceOperationsMapper;
    this.itemAddedOperationEventMessageMapper = itemAddedOperationEventMessageMapper;
  }

  @Override
  protected ResourceOperationsMapper resourceOperationsMapper() {
    return this.addItemOnResourceOperationsMapper;
  }

  @Override
  protected OperationEventFactoriesMapper operationEventDefinitionMapper() {
    return this.itemAddedOperationEventMessageMapper;
  }

  @Override
  protected String creationalPolicy() {
    return CommandHandlerDefinitionDTO.CreationPolicy.NEVER;
  }
}
