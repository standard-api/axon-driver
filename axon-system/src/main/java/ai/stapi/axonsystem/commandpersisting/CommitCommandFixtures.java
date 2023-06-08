package ai.stapi.axonsystem.commandpersisting;

import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.graphsystem.messaging.command.AbstractCommand;

public class CommitCommandFixtures extends AbstractCommand<UniqueIdentifier> {

  public static final String TARGET_AGGREGATE_IDENTIFIER = "CommitCommandFixtures";
  public static final String SERIALIZATION_TYPE = "CommitCommandFixtures";

  private String outputDirectoryPath;

  protected CommitCommandFixtures() {
  }

  public CommitCommandFixtures(String outputDirectoryPath) {
    super(new UniqueIdentifier(TARGET_AGGREGATE_IDENTIFIER), SERIALIZATION_TYPE);
    this.outputDirectoryPath = outputDirectoryPath;
  }

  public String getOutputDirectoryPath() {
    return outputDirectoryPath;
  }
}
