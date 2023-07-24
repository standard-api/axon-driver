package ai.stapi.axonsystemplugin.commandpersisting;

import ai.stapi.axonsystem.commandpersisting.CommandMessageStore;
import ai.stapi.axonsystemplugin.exampleimplmentation.CreateNewExampleNodeCommand;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import ai.stapi.test.domain.DomainTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;


@ActiveProfiles("dev")
class CommitCommandFixturesLineRunnerTest extends DomainTestCase {
    
    private final CommandMessageStore commandMessageStore;

    public CommitCommandFixturesLineRunnerTest(
        @Autowired CommandMessageStore commandMessageStore
    ) {
        this.commandMessageStore = commandMessageStore;
    }

    @BeforeEach
    void before() {
        this.commandMessageStore.wipeAll();
    }

    @Test
    public void itWorks() {
        this.givenCommandIsDispatched(
            new CreateNewExampleNodeCommand(
                UniversallyUniqueIdentifier.randomUUID(),
                "ExampleNodeType",
                "Example Name"
            )
        );

        var actualPath = this.getActualOutputDirectoryPath();
        var actualOutputPath = CommitCommandFixturesLineRunner.getOutputDirectoryPath(
                String.format("_outputPath:%s", actualPath)
        );
        Assertions.assertEquals(actualPath, actualOutputPath);
    }

    @NotNull
    private String getActualOutputDirectoryPath() {
        return this.getLocationFilePath() + "/output";
    }
}