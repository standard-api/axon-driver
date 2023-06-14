package ai.stapi.graphql;

import ai.stapi.graphql.generateGraphQlSchema.PrintGraphQlSchema;
import java.util.Arrays;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;


public class PrintGraphQLCommandLineRunner implements CommandLineRunner {

  private final CommandGateway commandGateway;

  private final ApplicationContext applicationContext;

  public PrintGraphQLCommandLineRunner(
      CommandGateway commandGateway,
      ApplicationContext applicationContext
  ) {
    this.commandGateway = commandGateway;
    this.applicationContext = applicationContext;
  }

  @Override
  public void run(String... args) {
    if (args.length == 0) {
      throw new RuntimeException("Please specify path where to print schema as command line argument.");
    }
    var path = Arrays.stream(args)
        .filter(arg -> arg.startsWith("_schemaOutputPath:"))
        .map(arg -> arg.replace("_schemaOutputPath:", ""))
        .findFirst();

    var command = new PrintGraphQlSchema(path.orElse(args[0]));
    this.commandGateway.sendAndWait(command);
    var exitCode = SpringApplication.exit(this.applicationContext, () -> 0);
    System.exit(exitCode);
  }
}
