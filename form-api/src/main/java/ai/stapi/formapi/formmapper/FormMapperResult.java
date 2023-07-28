package ai.stapi.formapi.formmapper;

import java.util.Map;

public class FormMapperResult {
    
    private final Map<String, Object> formSchema;
    private final Map<String, Object> uiSchema;
    private final Map<String, Object> formData;

    public FormMapperResult(
        Map<String, Object> formSchema, 
        Map<String, Object> uiSchema, 
        Map<String, Object> formData
    ) {
        this.formSchema = formSchema;
        this.uiSchema = uiSchema;
        this.formData = formData;
    }

    public Map<String, Object> getFormSchema() {
        return formSchema;
    }

    public Map<String, Object> getUiSchema() {
        return uiSchema;
    }

    public Map<String, Object> getFormData() {
        return formData;
    }
    
    
}
