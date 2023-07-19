package ai.stapi.axonsystemplugin.commandpersisting;

import ai.stapi.axonsystem.commandpersisting.CommitCommandFixtures;
import java.util.Arrays;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class CommitCommandFixturesLineRunner implements CommandLineRunner {

  private final CommandGateway commandGateway;
  private final ApplicationContext applicationContext;

  public CommitCommandFixturesLineRunner(
      CommandGateway commandGateway,
      ApplicationContext applicationContext
  ) {
    this.commandGateway = commandGateway;
    this.applicationContext = applicationContext;
  }

  @Override
  public void run(String... args) throws Exception {
    if (args.length == 0) {
      throw new RuntimeException("Please specify path where to commit persisted commands as command line argument.");
    }
    var path = Arrays.stream(args)
        .filter(arg -> arg.startsWith("_outputPath:"))
        .map(arg -> arg.replace("_outputPath:", ""))
        .findFirst();
    
    this.commandGateway.sendAndWait(new CommitCommandFixtures(path.orElse(args[0])));
    var exitCode = SpringApplication.exit(this.applicationContext, () -> 0);
    System.exit(exitCode);
  }
}
