package ai.stapi.formapi;

import ai.stapi.formapi.formmapper.FormMapper;
import java.util.Objects;

import ai.stapi.formapi.formmapper.FormMapperResult;
import org.springframework.web.bind.annotation.*;

@RestController
public class FormEndpoint {

  private final FormMapper formMapper;

  public FormEndpoint(FormMapper formMapper) {
    this.formMapper = formMapper;
  }

  @PostMapping("/form/{operationId}")
  @ResponseBody
  public FormMapperResult form(
      @PathVariable String operationId,
      @RequestBody(required = false) FormRequest formRequest
  ) {
    var finalRequest = Objects.requireNonNullElseGet(
        formRequest,
        () -> new FormRequest(null, null, null)
    );
    return this.formMapper.map(operationId, finalRequest);
  }
}
