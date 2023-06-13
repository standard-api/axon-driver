package ai.stapi.graphql.generateGraphQlSchema;

import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import ai.stapi.identity.UniqueIdentifier;

public class PrintGraphQlSchema extends AbstractCommand<UniqueIdentifier> {

  public static final String SERIALIZATION_TYPE = "PrintGraphQlSchema";
  public static final String TARGET_IDENTIFIER = "PrintGraphQlSchema";

  private String outputPath;

  protected PrintGraphQlSchema() {
    super(new UniqueIdentifier(TARGET_IDENTIFIER), SERIALIZATION_TYPE);
  }

  public PrintGraphQlSchema(String outputPath) {
    super(new UniqueIdentifier(TARGET_IDENTIFIER), SERIALIZATION_TYPE);
    this.outputPath = outputPath;
  }

  public String getOutputPath() {
    return outputPath;
  }
}
