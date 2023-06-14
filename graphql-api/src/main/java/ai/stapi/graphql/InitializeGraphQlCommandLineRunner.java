package ai.stapi.graphql;

import ai.stapi.graphql.graphqlJava.graphQLProvider.GraphQLProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

public class InitializeGraphQlCommandLineRunner implements CommandLineRunner {

  private final GraphQLProvider graphQLProvider;

  private final Logger logger;

  public InitializeGraphQlCommandLineRunner(
      GraphQLProvider graphQLProvider
  ) {
    this.graphQLProvider = graphQLProvider;
    this.logger = LoggerFactory.getLogger(InitializeGraphQlCommandLineRunner.class);
  }

  @Override
  public void run(String... args) {
    try {
      this.graphQLProvider.initialize();
    } catch (Exception exception) {
      this.logger.error("unable to generate graphql schema", exception);
    }
  }
}
