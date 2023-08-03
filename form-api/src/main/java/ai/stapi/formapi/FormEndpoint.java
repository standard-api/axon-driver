package ai.stapi.formapi;

import ai.stapi.formapi.forminfo.FormInfo;
import ai.stapi.formapi.forminfo.FormInfoMapper;
import ai.stapi.formapi.formmapper.FormMapper;
import ai.stapi.formapi.formmapper.FormMapperResult;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
public class FormEndpoint {

  private final FormMapper formMapper;
  private final FormInfoMapper formInfoMapper;

  public FormEndpoint(FormMapper formMapper, FormInfoMapper formInfoMapper) {
    this.formMapper = formMapper;
    this.formInfoMapper = formInfoMapper;
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

  @GetMapping("/form/info/{operationId}")
  @ResponseBody
  public FormInfo formInfo(@PathVariable String operationId) {
    return this.formInfoMapper.map(operationId);
  }
}
