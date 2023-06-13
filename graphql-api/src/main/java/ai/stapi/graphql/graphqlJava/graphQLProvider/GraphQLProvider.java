package ai.stapi.graphql.graphqlJava.graphQLProvider;

import graphql.GraphQL;
import graphql.schema.GraphQLSchema;

public interface GraphQLProvider {

  void initialize();

  void reinitialize();

  boolean isInitialized();

  GraphQL getGraphQL();

  GraphQLSchema getGraphQLSchema();
}
