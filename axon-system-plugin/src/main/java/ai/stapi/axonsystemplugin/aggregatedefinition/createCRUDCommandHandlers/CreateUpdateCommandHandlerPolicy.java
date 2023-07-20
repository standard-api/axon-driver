package ai.stapi.axonsystemplugin.aggregatedefinition.createCRUDCommandHandlers;

import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO.CreationPolicy;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.OperationEventFactoriesMapper;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.UpdatedOperationEventFactoriesMapper;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.ResourceOperationsMapper;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.UpdateResourceOperationMapper;
import ai.stapi.schema.structureSchemaMapper.StructureDefinitionToSSMapper;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;

public class CreateUpdateCommandHandlerPolicy extends AbstractCreateCRUDCommandHandlerPolicy {

  private final UpdateResourceOperationMapper updateResourceOperationMapper;
  private final UpdatedOperationEventFactoriesMapper updatedOperationEventFactoriesMapper;

  public CreateUpdateCommandHandlerPolicy(
      CommandGateway commandGateway,
      StructureSchemaFinder structureSchemaFinder,
      ObjectMapper objectMapper,
      StructureDefinitionToSSMapper structureDefinitionToSSMapper,
      UpdateResourceOperationMapper updateResourceOperationMapper,
      UpdatedOperationEventFactoriesMapper updatedOperationEventFactoriesMapper
  ) {
    super(commandGateway, structureSchemaFinder, objectMapper, structureDefinitionToSSMapper);
    this.updateResourceOperationMapper = updateResourceOperationMapper;
    this.updatedOperationEventFactoriesMapper = updatedOperationEventFactoriesMapper;
  }

  @Override
  protected ResourceOperationsMapper resourceOperationsMapper() {
    return this.updateResourceOperationMapper;
  }

  @Override
  protected OperationEventFactoriesMapper operationEventDefinitionMapper() {
    return this.updatedOperationEventFactoriesMapper;
  }

  @Override
  protected String creationalPolicy() {
    return CreationPolicy.NEVER;
  }
}
