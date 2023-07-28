package ai.stapi.formapi;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FormRequest {
  
  @Nullable
  private final String resourceId;
  
  @Nullable
  private final Map<String, Object> targets;
  
  @Nullable
  private final Boolean omitExtension;

  public FormRequest(
      @Nullable String resourceId, 
      @Nullable Map<String, Object> targets, 
      @Nullable Boolean omitExtension
  ) {
    this.resourceId = resourceId;
    this.targets = targets;
    this.omitExtension = omitExtension;
  }

  @Nullable
  public String getResourceId() {
    return resourceId;
  }

  public Map<String, Object> getTargets() {
    return targets == null ? new HashMap<>() : targets;
  }

  public Boolean getOmitExtension() {
    return omitExtension == null || omitExtension;
  }
}
