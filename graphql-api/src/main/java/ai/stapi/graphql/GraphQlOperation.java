package ai.stapi.graphql;

import java.util.HashMap;
import java.util.Map;

public class GraphQlOperation {

  private String operationName;
  private String query;

  private Map<String, Object> variables;

  protected GraphQlOperation() {
  }

  public GraphQlOperation(String operationName, String query, Map<String, Object> variables) {
    this.operationName = operationName;
    this.query = query;
    this.variables = variables;
  }

  public GraphQlOperation(String operationName, String query) {
    this(operationName, query, new HashMap<>());
  }

  public String getQuery() {
    return query;
  }

  public String getOperationName() {
    return operationName;
  }

  public Map<String, Object> getVariables() {
    return variables;
  }
}
