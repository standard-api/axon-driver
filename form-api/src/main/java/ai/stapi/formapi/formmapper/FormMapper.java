package ai.stapi.formapi.formmapper;

import ai.stapi.formapi.FormRequest;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;

import java.util.HashMap;

public class FormMapper {

    private final JsonSchemaMapper jsonSchemaMapper;
    private final UISchemaLoader uiSchemaLoader;
    private final FormDataLoader formDataLoader;
    private final OperationDefinitionProvider operationDefinitionProvider;

    public FormMapper(
        JsonSchemaMapper jsonSchemaMapper, 
        UISchemaLoader uiSchemaLoader, 
        FormDataLoader formDataLoader, 
        OperationDefinitionProvider operationDefinitionProvider
    ) {
        this.jsonSchemaMapper = jsonSchemaMapper;
        this.uiSchemaLoader = uiSchemaLoader;
        this.formDataLoader = formDataLoader;
        this.operationDefinitionProvider = operationDefinitionProvider;
    }
    
    public FormMapperResult map(String operationId, FormRequest formRequest) {
        var operation = this.operationDefinitionProvider.provide(operationId);
        var jsonSchema = this.jsonSchemaMapper.map(operation, formRequest.getOmitExtension());
        var uiSchema = this.uiSchemaLoader.load(operation);
        return new FormMapperResult(
            jsonSchema,
            uiSchema,
            formRequest.getResourceId() == null ? new HashMap<>() : this.formDataLoader.load(
                operation,
                formRequest.getResourceId(),
                formRequest.getTargets()
            )
        );
    }
}
