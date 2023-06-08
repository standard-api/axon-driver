package ai.stapi.axonsystemplugin.structuredefinition.configure;

import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;

public class ConfigureStructureDefinition extends AbstractCommand<UniqueIdentifier> {

  public static final String SERIALIZATION_TYPE = "ConfigureStructureDefinition";
  public static final String TARGET_AGGREGATE_IDENTIFIER = "ConfigureStructureDefinitionHandler";
  private StructureDefinitionData structureDefinitionData;

  private ConfigureStructureDefinition() {
  }

  public ConfigureStructureDefinition(StructureDefinitionData structureDefinitionData) {
    super(new UniqueIdentifier(TARGET_AGGREGATE_IDENTIFIER), SERIALIZATION_TYPE);
    this.structureDefinitionData = structureDefinitionData;
  }

  public StructureDefinitionData getStructureDefinitionDTO() {
    return structureDefinitionData;
  }
}
