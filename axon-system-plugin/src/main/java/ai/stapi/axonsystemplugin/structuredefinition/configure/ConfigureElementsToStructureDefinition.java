package ai.stapi.axonsystemplugin.structuredefinition.configure;

import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import java.util.List;

public class ConfigureElementsToStructureDefinition extends AbstractCommand<UniqueIdentifier> {

  public static final String SERIALIZATION_TYPE = "ConfigureElementsToStructureDefinition";
  public static final String TARGET_AGGREGATE_IDENTIFIER = "ConfigureStructureDefinitionHandler";

  private StructureDefinitionId structureDefinitionId;

  private List<ElementDefinition> elementDefinitions;

  private ConfigureElementsToStructureDefinition() {
  }

  public ConfigureElementsToStructureDefinition(
      StructureDefinitionId structureDefinitionId,
      List<ElementDefinition> elementDefinitions
  ) {
    super(new UniqueIdentifier(TARGET_AGGREGATE_IDENTIFIER), SERIALIZATION_TYPE);
    this.structureDefinitionId = structureDefinitionId;
    this.elementDefinitions = elementDefinitions;
  }

  public StructureDefinitionId getStructureDefinitionId() {
    return structureDefinitionId;
  }

  public List<ElementDefinition> getElementDefinitions() {
    return elementDefinitions;
  }
}
