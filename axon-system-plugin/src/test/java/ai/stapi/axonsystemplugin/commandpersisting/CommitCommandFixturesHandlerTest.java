package ai.stapi.axonsystemplugin.commandpersisting;

import ai.stapi.axonsystem.commandpersisting.CommandMessageStore;
import ai.stapi.axonsystem.commandpersisting.CommitCommandFixtures;
import ai.stapi.axonsystemplugin.exampleimplmentation.ChangeExampleNodeExampleQuantity;
import ai.stapi.axonsystemplugin.exampleimplmentation.CreateNewExampleNodeCommand;
import ai.stapi.axonsystemplugin.exampleimplmentation.configuration.ExampleModuleDefinitionsLoader;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@StructureDefinitionScope(ExampleModuleDefinitionsLoader.SCOPE)
@ActiveProfiles("dev")
class CommitCommandFixturesHandlerTest extends DomainTestCase {

  @Autowired
  private CommandMessageStore commandMessageStore;

  @Autowired
  private CommandGateway commandGateway;

  static void deleteDirectory(File dir) {
    File[] files = dir.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          file.delete();
        }
      }
    }
    dir.delete();
  }

  @BeforeEach
  void before() {
    this.commandMessageStore.wipeAll();
    var dir = new File(this.getActualOutputDirectoryPath());
    deleteDirectory(dir);
  }

  @AfterEach
  void after() {
    var dir = new File(this.getActualOutputDirectoryPath());
    deleteDirectory(dir);
  }

  @Test
  void itCanCommitHandledCommand() {
    var commandName1 = new UniversallyUniqueIdentifier(UUID.randomUUID());
    var commandName2 = new UniversallyUniqueIdentifier(UUID.randomUUID());
    var commandName3 = new UniversallyUniqueIdentifier(UUID.randomUUID());
    this.givenCommandIsDispatched(
        new CreateNewExampleNodeCommand(
            commandName1,
            "ExampleNodeType",
            "Example Name"
        )
    );
    this.givenCommandIsDispatched(
        new CreateNewExampleNodeCommand(
            commandName2,
            "ExampleNodeType",
            "Example Name 2"
        )
    );
    this.givenCommandIsDispatched(
        new CreateNewExampleNodeCommand(
            commandName3,
            "ExampleNodeType2",
            "Example Name 3"
        )
    );

    var actualPath = this.getActualOutputDirectoryPath();
    this.whenCommandIsDispatched(
        new CommitCommandFixtures(actualPath)
    );
    this.thenFilesExistAndAreValid(
        CreateNewExampleNodeCommand.class.getCanonicalName(),
        List.of(commandName1, commandName2, commandName3)
    );
  }

  @Test
  void itWillNotCommitFailedCommand() {
    var expectedFileId = new UniversallyUniqueIdentifier(UUID.randomUUID());
    this.commandGateway.send(
        new CreateNewExampleNodeCommand(
            expectedFileId,
            "ExampleNodeType",
            "Example Name"
        )
    );
    this.commandGateway.send(
        new ChangeExampleNodeExampleQuantity(
            new UniversallyUniqueIdentifier(UUID.randomUUID()),
            50
        )
    );

    var actualPath = this.getActualOutputDirectoryPath();
    this.whenCommandIsDispatched(
        new CommitCommandFixtures(actualPath)
    );
    this.thenFilesExistAndAreValid(
        CreateNewExampleNodeCommand.class.getCanonicalName(),
        List.of(expectedFileId)
    );
    this.thenFilesExistAndAreValid(
        ChangeExampleNodeExampleQuantity.class.getCanonicalName(),
        List.of()
    );
  }

  private void thenFilesExistAndAreValid(
      String commandType,
      List<UniqueIdentifier> commandIds
  ) {
    var restOfName = "profile.custom.json";
    var dateRegex = Pattern.compile("[0-9]{12}");

    var actualPath = this.getActualOutputDirectoryPath();

    var dir = new File(actualPath + "/" + commandType);
    var array = dir.listFiles();
    var files = Arrays.stream(array == null ? new File[] {} : array)
        .filter(File::isFile)
        .toList();

    Assertions.assertEquals(
        commandIds.size(),
        files.size(),
        "Expected number of command ids and output file count does not match."
    );
    files.forEach(file -> {
      var fileName = file.getName();
      var nameParts = fileName.split("\\.");
      Assertions.assertEquals(
          5,
          nameParts.length,
          "File name is invalid, expected 5 parts divided by '.'" +
              "\nActual name: " + fileName
      );
      var datePart = nameParts[0];
      Assertions.assertTrue(
          dateRegex.matcher(datePart).matches(),
          "First name part should be valid date in the format 'yyMMddhhmmss'" +
              "\nActual: " + datePart
      );
      var commandNamePart = nameParts[1];
      Assertions.assertTrue(
          commandIds.stream().anyMatch(id -> id.getId().equals(commandNamePart)),
          "Second name part should be in expected command ids." +
              "\nActual: " + commandNamePart
      );
      Assertions.assertEquals(
          restOfName,
          String.join(".", Arrays.stream(nameParts).toList().subList(2, 5)),
          "Rest of file name parts are invalid."
      );
    });
  }

  @NotNull
  private String getActualOutputDirectoryPath() {
    return this.getLocationFilePath() + "/actualFixtures";
  }
}