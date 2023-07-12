package ai.stapi.axonsystemplugin.fixtures.fixtureCommandsGenerator;

import ai.stapi.axonsystemplugin.exampleimplmentation.configuration.ExampleModuleDefinitionsLoader;
import ai.stapi.axonsystemplugin.fixtures.ExampleImplementations.ExampleFixtureCommandsGenerator;
import ai.stapi.axonsystemplugin.fixtures.ExampleImplementations.ExampleFixtureTags;
import ai.stapi.graphsystem.fixtures.fixtureCommandsGenerator.FixtureCommandsGeneratorResult;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


@StructureDefinitionScope(ExampleModuleDefinitionsLoader.SCOPE)
public class FixtureCommandsGeneratorTest extends DomainTestCase {

  @Test
  void itCanReturnCorrectFixtureCommandGeneratorResult() {
    var generator = new ExampleFixtureCommandsGenerator();
    var actualResult = generator.generate(Set.of());
    this.thenObjectApprovedWithShownIds(actualResult);
  }

  @Test
  void itCanSerializeAndDeserializeDefinedCommands() {
    var generator = new ExampleFixtureCommandsGenerator();
    var actualResult = generator.generate(Set.of());
    var mapper = new ObjectMapper();
    try {
      var serialized = mapper.writeValueAsString(actualResult);
      var deserialized = mapper.readValue(serialized, FixtureCommandsGeneratorResult.class);
      this.thenObjectApprovedWithShownIds(deserialized);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void itShouldReturnDefinedFixtureTags() {
    var generator = new ExampleFixtureCommandsGenerator();
    var actualTags = generator.getFixtureTags();
    Assertions.assertEquals(2, actualTags.size());
    Assertions.assertEquals(ExampleFixtureTags.EXAMPLE_TAG, actualTags.get(0));
    Assertions.assertEquals(ExampleFixtureTags.EXAMPLE_ANOTHER_TAG, actualTags.get(1));
  }
}