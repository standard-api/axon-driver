package ai.stapi.axonsystemplugin.fixtures;

import ai.stapi.graphsystem.fixtures.fixtureCommandsGenerator.FixtureCommandsGeneratorResult;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FixtureCommandsApplier {

  private final CommandGateway commandGateway;
  private final Logger logger;

  public FixtureCommandsApplier(
      CommandGateway commandGateway
  ) {
    this.commandGateway = commandGateway;
    this.logger = LoggerFactory.getLogger(FixtureCommandsApplier.class);
  }

  public void apply(FixtureCommandsGeneratorResult result) {
    result.getCommandDefinitions().forEach(definition -> {
      this.logger.info(
          "Applying fixture command of type: " + definition.getName() +
              "\nLocated in generator: " + result.getGeneratorClassName()
      );
      this.commandGateway.sendAndWait(definition.getCommand());
    });
  }
}
