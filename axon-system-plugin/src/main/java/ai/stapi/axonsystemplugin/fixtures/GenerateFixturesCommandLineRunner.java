package ai.stapi.axonsystemplugin.fixtures;

import ai.stapi.axonsystemplugin.DefaultGraphProjection;
import java.time.Duration;
import java.util.List;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

public class GenerateFixturesCommandLineRunner implements CommandLineRunner {

  private final CommandGateway commandGateway;
  private final DefaultGraphProjection graphProjection;
  private final ApplicationContext applicationContext;
  private final Logger logger;

  public GenerateFixturesCommandLineRunner(
      CommandGateway commandGateway,
      DefaultGraphProjection graphProjection,
      ApplicationContext applicationContext
  ) {
    this.commandGateway = commandGateway;
    this.graphProjection = graphProjection;
    this.applicationContext = applicationContext;
    this.logger = LoggerFactory.getLogger(GenerateFixturesCommandLineRunner.class);
  }

  @Override
  public void run(String... args) throws Exception {
    this.logger.info("Going to synchronize structure fixtures");
    generate(
        Float.MIN_VALUE,
        Float.MAX_VALUE,
        args
    );

    var exitCode = SpringApplication.exit(this.applicationContext, () -> 0);
    System.exit(exitCode);
  }

  private void generate(float minPriority, float maxPriority, String[] args) {
    var command = new GenerateFixtures(
        List.of(args),
        minPriority,
        maxPriority
    );
    this.logger.info("Sending generate fixtures command");
    this.commandGateway.send(command);
    var lastEventTime = this.graphProjection.getLastEventTime();
    while (true) {
      try {
        Thread.sleep(15000);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
      var newTime = this.graphProjection.getLastEventTime();
      if (Duration.between(lastEventTime, newTime).getSeconds() == 0) {
        return;
      }
      lastEventTime = newTime;
    }
  }
}
