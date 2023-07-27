package ai.stapi.formapi;

import ai.stapi.formapi.formmapper.FormDataLoader;
import ai.stapi.formapi.formmapper.JsonSchemaMapper;
import ai.stapi.formapi.formmapper.UISchemaLoader;

import java.util.HashMap;
import java.util.Map;

import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

  @GetMapping({
      "/form/{operationId}",
      "/form/{operationId}/{resourceId}",
  })
  @ResponseBody
  public Map<String, Object> form(
      @PathVariable String operationId,
      @PathVariable(required = false) String resourceId,
      @RequestParam(required = false) Map<String, Object> targets,
      @RequestParam(required = false, defaultValue = "true") Boolean omitExtensions
  ) {
    var operation = this.operationDefinitionProvider.provide(operationId);
    
    var formSchema = new HashMap<String, Object>(Map.of(
        "formSchema", this.jsonSchemaMapper.map(operation, omitExtensions),
        "uiSchema", this.uiSchemaLoader.load(operation)
    ));
    if (resourceId != null) {
      formSchema.put(
          "formData",
          this.formDataLoader.load(
              operation,
              resourceId,
              targets
          )
      );
    }
    return formSchema;
  }
}
