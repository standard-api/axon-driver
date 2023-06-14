package ai.stapi.graphql;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class GraphQlEndpoint {

  private final GraphQlExecutor graphQlExecutor;

  public GraphQlEndpoint(
      GraphQlExecutor graphQlExecutor
  ) {
    this.graphQlExecutor = graphQlExecutor;
  }

  @PostMapping("/graphql")
  @ResponseBody
  public Map<String, Object> graphQl(@RequestBody GraphQlOperation graphQLOperation) {
    var response = new LinkedHashMap<String, Object>();
    response.put("data", this.graphQlExecutor.execute(graphQLOperation));
    return response;
  }
}
