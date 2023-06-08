package ai.stapi.axonsystemplugin.fixtures.ExampleImplementations;

import ai.stapi.axonsystemplugin.exampleimplmentation.ChangeExampleNodeExampleQuantity;
import ai.stapi.graphsystem.fixtures.fixtureCommandsGenerator.SimpleFixtureCommandsGenerator;
import ai.stapi.graphsystem.messaging.command.Command;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExampleChangeFixtureCommandsGenerator extends SimpleFixtureCommandsGenerator {
  

  @Override
  public float getPriority() {
    return this.PRIORITY_DATA;
  }

  @Override
  public List<String> getFixtureTags() {
    return List.of(ExampleFixtureTags.EXAMPLE_CHANGE_TAG);
  }


  @Override
  protected List<Command> getCommands() {
    return List.of(
        new ChangeExampleNodeExampleQuantity(
            new UniversallyUniqueIdentifier(ExampleFixtureCommandsGenerator.EXAMPLE_ENTITY_ID),
            10
        )
    );
  }
}
