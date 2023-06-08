package ai.stapi.axonsystemplugin.aggregatedefinition.createCRUDCommandHandlers;

import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO.CreationPolicy;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.CreatedOperationEventFactoriesMapper;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.OperationEventFactoriesMapper;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.CreationalResourceOperationMapper;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.ResourceOperationsMapper;
import ai.stapi.schema.structureSchemaMapper.StructureDefinitionToSSMapper;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;

public class CreateCreationalCommandHandlerPolicy extends AbstractCreateCRUDCommandHandlerPolicy {

  private final CreationalResourceOperationMapper creationalResourceOperationMapper;
  private final CreatedOperationEventFactoriesMapper createdOperationEventDefinitionMapper;

  public CreateCreationalCommandHandlerPolicy(
      CommandGateway commandGateway,
      StructureSchemaFinder structureSchemaFinder,
      ObjectMapper objectMapper,
      StructureDefinitionToSSMapper structureDefinitionToSSMapper,
      CreationalResourceOperationMapper creationalResourceOperationMapper,
      CreatedOperationEventFactoriesMapper createdOperationEventDefinitionMapper
  ) {
    super(commandGateway, structureSchemaFinder, objectMapper, structureDefinitionToSSMapper);
    this.creationalResourceOperationMapper = creationalResourceOperationMapper;
    this.createdOperationEventDefinitionMapper = createdOperationEventDefinitionMapper;
  }

  @Override
  protected ResourceOperationsMapper resourceOperationsMapper() {
    return this.creationalResourceOperationMapper;
  }

  @Override
  protected OperationEventFactoriesMapper operationEventDefinitionMapper() {
    return this.createdOperationEventDefinitionMapper;
  }

  @Override
  protected String creationalPolicy() {
    return CreationPolicy.IF_MISSING;
  }
}
