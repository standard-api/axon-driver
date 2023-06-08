package ai.stapi.axonsystemplugin.structuredefinition.configure;

import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaProvider;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import java.util.stream.Collectors;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;

public class ConfigureStructureDefinitionHandler {

  private final EventGateway eventGateway;
  private final StructureSchemaProvider structureSchemaProvider;
  private final StructureSchemaFinder structureSchemaFinder;

  public ConfigureStructureDefinitionHandler(
      EventGateway eventGateway,
      StructureSchemaProvider structureSchemaProvider,
      StructureSchemaFinder structureSchemaFinder
  ) {
    this.eventGateway = eventGateway;
    this.structureSchemaProvider = structureSchemaProvider;
    this.structureSchemaFinder = structureSchemaFinder;
  }

  @CommandHandler
  public void handle(ConfigureStructureDefinition command) {
    var structureDefinitionDTO = command.getStructureDefinitionDTO();
    this.structureSchemaProvider.add(structureDefinitionDTO);
    this.eventGateway.publish(
        new StructureDefinitionConfigured(
            new StructureDefinitionId(structureDefinitionDTO.getId()),
            structureDefinitionDTO.getKind()
        )
    );
  }

  @CommandHandler
  public void handle(ConfigureElementsToStructureDefinition command) {
    var structureType = command.getStructureDefinitionId().getId();
    var existingStructure = this.structureSchemaFinder.getStructureType(
        structureType
    );
    var parentType = existingStructure.getParent();
    var elementDefinitions = command.getElementDefinitions();
    var newStructure = new StructureDefinitionData(
        existingStructure.getDefinitionType(),
        null,
        null,
        null,
        existingStructure.getKind(),
        existingStructure.isAbstract(),
        existingStructure.getDefinitionType(),
        null,
        parentType == null ? null : new UniqueIdentifier(parentType),
        elementDefinitions
    );
    this.structureSchemaProvider.add(newStructure);
    this.eventGateway.publish(
        new ElementsToStructureDefinitionConfigured(
            new StructureDefinitionId(structureType),
            existingStructure.getKind(),
            elementDefinitions.stream().map(ElementDefinition::getPath).collect(Collectors.toList())
        )
    );
  }
}
