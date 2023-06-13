package ai.stapi.graphql.graphqlJava.graphQLProvider;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

public class DummyGraphQLProvider implements GraphQLProvider {

  @Override
  public void initialize() {

  }

  @Override
  public void reinitialize() {

  }

  @Override
  public boolean isInitialized() {
    return false;
  }

  @Override
  public GraphQL getGraphQL() {
    return GraphQL.newGraphQL(GraphQLSchema.newSchema().build()).build();
  }

  @Override
  public GraphQLSchema getGraphQLSchema() {
    return GraphQLSchema.newSchema().build();
  }
}
