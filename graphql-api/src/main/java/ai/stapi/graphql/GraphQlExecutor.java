package ai.stapi.graphql;

import ai.stapi.graphql.graphqlJava.graphQLProvider.GraphQLProvider;
import graphql.ExecutionInput;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class GraphQlExecutor {

  private final GraphQLProvider schemaProvider;

  public GraphQlExecutor(GraphQLProvider schemaProvider) {
    this.schemaProvider = schemaProvider;
  }

  public Map<Object, Object> execute(GraphQlOperation graphQLOperation) {
    var executionInputBuilder = new ExecutionInput.Builder().query(graphQLOperation.getQuery());

    if (graphQLOperation.getOperationName() != null) {
      executionInputBuilder.operationName(graphQLOperation.getOperationName());
    }
    if (graphQLOperation.getVariables() != null) {
      executionInputBuilder.variables(graphQLOperation.getVariables());
    }
    var executionResult = this.schemaProvider.getGraphQL().execute(executionInputBuilder);
    return executionResult.getData();
  }

}
