package ai.stapi.axonsystemplugin.structuredefinition.configure;

import ai.stapi.graphsystem.messaging.event.Event;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;

public class StructureDefinitionConfigured implements Event {

  private StructureDefinitionId structureDefinitionId;
  private String kind;

  private StructureDefinitionConfigured() {
  }

  public StructureDefinitionConfigured(
      StructureDefinitionId structureDefinitionId,
      String kind
  ) {
    this.structureDefinitionId = structureDefinitionId;
    this.kind = kind;
  }

  public StructureDefinitionId getStructureDefinitionId() {
    return structureDefinitionId;
  }

  public String getKind() {
    return kind;
  }
}
