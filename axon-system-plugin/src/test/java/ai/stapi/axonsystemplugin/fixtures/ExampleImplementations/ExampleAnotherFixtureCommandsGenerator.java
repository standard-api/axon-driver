package ai.stapi.axonsystemplugin.fixtures.ExampleImplementations;

import ai.stapi.axonsystemplugin.exampleimplmentation.CreateNewExampleNodeCommand;
import ai.stapi.graphsystem.fixtures.fixtureCommandsGenerator.SimpleFixtureCommandsGenerator;
import ai.stapi.graphsystem.messaging.command.Command;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExampleAnotherFixtureCommandsGenerator extends SimpleFixtureCommandsGenerator {

  public static final String EXAMPLE_ANOTHER_ENTITY_ID = "2d82aa9a-64d9-4e68-a825-ecacca941201";
  

  @Override
  public float getPriority() {
    return this.PRIORITY_DATA;
  }

  @Override
  public List<String> getFixtureTags() {
    return List.of(ExampleFixtureTags.EXAMPLE_ANOTHER_TAG);
  }

  @Override
  protected List<Command> getCommands() {
    return List.of(
        new CreateNewExampleNodeCommand(
            new UniversallyUniqueIdentifier(EXAMPLE_ANOTHER_ENTITY_ID),
            "ExampleNodeType",
            "Example Another Name"
        )
    );
  }
}
