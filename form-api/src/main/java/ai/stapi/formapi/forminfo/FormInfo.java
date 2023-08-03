package ai.stapi.formapi.forminfo;

import java.util.Map;
import java.util.Set;

public class FormInfo {
    
    private final Map<String, Set<String>> possibleTargets;
    private final String resourceType;
    private final Boolean requiresResourceId;

    public FormInfo(
        Map<String, Set<String>> possibleTargets, 
        String resourceType, 
        Boolean requiresResourceId
    ) {
        this.possibleTargets = possibleTargets;
        this.resourceType = resourceType;
        this.requiresResourceId = requiresResourceId;
    }

    public Map<String, Set<String>> getPossibleTargets() {
        return possibleTargets;
    }

    public String getResourceType() {
        return resourceType;
    }

    public Boolean getRequiresResourceId() {
        return requiresResourceId;
    }
}
