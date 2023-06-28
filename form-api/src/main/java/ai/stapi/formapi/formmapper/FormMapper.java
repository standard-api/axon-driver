package ai.stapi.formapi.formmapper;

import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import java.util.Map;

public class FormMapper {
  
    private final StructureSchemaFinder structureSchemaFinder;

    public FormMapper(StructureSchemaFinder structureSchemaFinder) {
        this.structureSchemaFinder = structureSchemaFinder;
    }

    public Map<String, Object> map(OperationDefinitionDTO operationDefinitionDTO) {
        return null;
      
    }
}
