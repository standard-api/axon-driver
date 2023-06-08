package ai.stapi.axonsystemplugin.structuredefinition.configure;

import ai.stapi.graphsystem.messaging.event.Event;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import java.util.List;

public class ElementsToStructureDefinitionConfigured implements Event {

  private StructureDefinitionId structureDefinitionId;
  private String kind;

  private List<String> elementPaths;

  private ElementsToStructureDefinitionConfigured() {
  }

  public ElementsToStructureDefinitionConfigured(
      StructureDefinitionId structureDefinitionId,
      String kind,
      List<String> elementPaths
  ) {
    this.structureDefinitionId = structureDefinitionId;
    this.kind = kind;
    this.elementPaths = elementPaths;
  }

  public StructureDefinitionId getStructureDefinitionId() {
    return structureDefinitionId;
  }

  public String getKind() {
    return kind;
  }

  public List<String> getElementPaths() {
    return elementPaths;
  }
}
