package ai.stapi.formapi.formmapper;

import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;
import java.util.Map;

public class NullFormDataLoader implements FormDataLoader {

  @Override
  public Map<String, Object> load(
      OperationDefinitionDTO operationDefinitionDTO,
      String resourceId,
      Map<String, Object> possibleStartIds
  ) {
    return Map.of();
  }
}
