package ai.stapi.formapi;

import ai.stapi.formapi.formmapper.FormDataLoader;
import ai.stapi.formapi.formmapper.JsonSchemaMapper;
import ai.stapi.formapi.formmapper.UISchemaLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import org.springframework.web.bind.annotation.*;

@RestController
public class FormEndpoint {

  private final JsonSchemaMapper jsonSchemaMapper;
  private final UISchemaLoader uiSchemaLoader;
  private final FormDataLoader formDataLoader;
  private final OperationDefinitionProvider operationDefinitionProvider;

  public FormEndpoint(
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

  @PostMapping("/form/{operationId}")
  @ResponseBody
  public Map<String, Object> form(
      @PathVariable String operationId,
      @RequestBody(required = false) FormRequest formRequest
  ) {
    var operation = this.operationDefinitionProvider.provide(operationId);
    var finalRequest = Objects.requireNonNullElseGet(
        formRequest, 
        () -> new FormRequest(null, null, null)
    );
    var formSchema = new HashMap<String, Object>(Map.of(
        "formSchema", this.jsonSchemaMapper.map(operation, finalRequest.getOmitExtension()),
        "uiSchema", this.uiSchemaLoader.load(operation)
    ));
    if (finalRequest.getResourceId() != null) {
      formSchema.put(
          "formData",
          this.formDataLoader.load(
              operation,
              finalRequest.getResourceId(),
              finalRequest.getTargets()
          )
      );
    }
    return formSchema;
  }
}
