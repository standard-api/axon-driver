package ai.stapi.axonsystemplugin.commandpersisting;

import ai.stapi.axonsystem.commandpersisting.CommitCommandFixtures;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("commit-command-fixtures")
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
    var outputDirectoryPath = System.getProperty("user.dir")
        + "/src/main/java/com/geniolab/application/Core/Fixtures/__generated__";
    this.commandGateway.sendAndWait(new CommitCommandFixtures(outputDirectoryPath));
    var exitCode = SpringApplication.exit(this.applicationContext, () -> 0);
    System.exit(exitCode);
  }
}
