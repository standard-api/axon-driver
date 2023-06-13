package ai.stapi.graphql.generateGraphQlSchema;

import ai.stapi.test.domain.DomainTestCase;
import java.io.File;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PrintGraphQlSchemaCommandHandlerTest extends DomainTestCase {

  @Autowired
  private CommandGateway commandHandler;

  @Test
  void itShouldPrintGraphQLSchemaAtSpecifiedOutputPath() {
    var outputPath = this.getLocationFilePath() + "/actual.graphql";
    File f = new File(outputPath);
    if (f.exists()) {
      f.delete();
    }
    this.commandHandler.sendAndWait(new PrintGraphQlSchema(outputPath));

    Assertions.assertTrue(f.exists());
    if (f.exists()) {
      f.delete();
    }
  }
}