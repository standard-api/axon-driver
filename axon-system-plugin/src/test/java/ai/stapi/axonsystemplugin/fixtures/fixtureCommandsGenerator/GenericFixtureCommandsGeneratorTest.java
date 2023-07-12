package ai.stapi.axonsystemplugin.fixtures.fixtureCommandsGenerator;

import ai.stapi.axonsystemplugin.exampleimplmentation.configuration.ExampleModuleDefinitionsLoader;
import ai.stapi.axonsystemplugin.fixtures.ExampleImplementations.ExampleAnotherFixtureCommandsGenerator;
import ai.stapi.axonsystemplugin.fixtures.ExampleImplementations.ExampleFileFixtureCommandsGenerator;
import ai.stapi.axonsystemplugin.fixtures.ExampleImplementations.ExampleFixtureCommandsGenerator;
import ai.stapi.axonsystemplugin.fixtures.ExampleImplementations.ExampleFixtureTags;
import ai.stapi.graphsystem.fixtures.fixtureCommandsGenerator.FixtureCommandsGenerator;
import ai.stapi.graphsystem.fixtures.fixtureCommandsGenerator.GenericFixtureCommandsGenerator;
import ai.stapi.schema.adHocLoaders.FileLoader;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(ExampleModuleDefinitionsLoader.SCOPE)
public class GenericFixtureCommandsGeneratorTest extends DomainTestCase {

  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private FileLoader fileFixtureLoader;

  @Test
  void itShouldReturnNoCommandsWhenNoFixtureTagProvided() {
    var exampleGenerator = new ExampleFixtureCommandsGenerator();
    var genericGenerator = new GenericFixtureCommandsGenerator(
        List.of(exampleGenerator)
    );
    var actualResults = genericGenerator.generate(
        List.of(),
        List.of(),
        Set.of(),
        0,
        FixtureCommandsGenerator.PRIORITY_STRUCTURES
    ).toList();
    this.thenObjectApprovedWithShownIds(actualResults);
  }

  @Test
  void itShouldReturnNoCommandsWhenUnusedFixtureTagProvided() {
    var exampleGenerator = new ExampleFixtureCommandsGenerator();
    var genericGenerator = new GenericFixtureCommandsGenerator(
        List.of(exampleGenerator)
    );
    var providedTags = List.of(ExampleFixtureTags.EXAMPLE_UNUSED_TAG);
    var actualResults = genericGenerator.generate(
        providedTags,
        List.of(),
        Set.of(),
        0,
        FixtureCommandsGenerator.PRIORITY_STRUCTURES
    ).toList();
    this.thenObjectApprovedWithShownIds(actualResults);
  }

  @Test
  void itShouldReturnCorrectCommandsWhenExampleFixtureTagProvided() {
    var exampleGenerator = new ExampleFixtureCommandsGenerator();
    var genericGenerator = new GenericFixtureCommandsGenerator(
        List.of(exampleGenerator)
    );
    var actualResults = genericGenerator.generate(
        List.of(ExampleFixtureTags.EXAMPLE_TAG),
        List.of(),
        Set.of(),
        0,
        FixtureCommandsGenerator.PRIORITY_STRUCTURES
    ).toList();
    this.thenObjectApprovedWithShownIds(actualResults);
  }

  @Test
  void itShouldLoadFixturesFromFile() {
    var exampleGenerator =
        new ExampleFileFixtureCommandsGenerator(this.objectMapper, fileFixtureLoader);
    var genericGenerator = new GenericFixtureCommandsGenerator(
        List.of(exampleGenerator)
    );
    var actualResults = genericGenerator.generate(
        List.of(
            ExampleFixtureTags.EXAMPLE_FILE_LOADER_TAG
        ),
        List.of(),
        Set.of(),
        0,
        FixtureCommandsGenerator.PRIORITY_STRUCTURES
    ).toList();
    this.thenObjectApprovedWithShownIds(actualResults);
  }

  @Test
  void itShouldLoadOnlyFixturesFromFileWhichHaveNotBeenExecutedYet() {
    var exampleGenerator =
        new ExampleFileFixtureCommandsGenerator(this.objectMapper, fileFixtureLoader);
    var genericGenerator = new GenericFixtureCommandsGenerator(
        List.of(exampleGenerator)
    );
    var actualResults = genericGenerator.generate(
        List.of(
            ExampleFixtureTags.EXAMPLE_FILE_LOADER_TAG
        ),
        List.of(),
        Set.of("CreateStructureDefinition/230223011314.example.profile.canonical.json"),
        0,
        FixtureCommandsGenerator.PRIORITY_STRUCTURES
    ).toList();
    this.thenObjectApprovedWithShownIds(actualResults);
  }

  @Test
  void itShouldReturnCorrectCommandsOnlyOnceWhenMoreTagsSupportedByOneGeneratorProvided() {
    var exampleGenerator = new ExampleFixtureCommandsGenerator();
    var genericGenerator = new GenericFixtureCommandsGenerator(
        List.of(exampleGenerator)
    );
    var providedTags =
        List.of(ExampleFixtureTags.EXAMPLE_TAG, ExampleFixtureTags.EXAMPLE_ANOTHER_TAG);
    var actualResults = genericGenerator.generate(
        providedTags,
        List.of(),
        Set.of(),
        0,
        FixtureCommandsGenerator.PRIORITY_STRUCTURES
    ).toList();
    this.thenObjectApprovedWithShownIds(actualResults);
  }

  @Test
  void itShouldReturnCorrectCommandsWhenTagIsSupportedByMoreGenerators() {
    var exampleGenerator = new ExampleFixtureCommandsGenerator();
    var exampleGenerator2 = new ExampleAnotherFixtureCommandsGenerator();
    var genericGenerator = new GenericFixtureCommandsGenerator(
        List.of(
            exampleGenerator,
            exampleGenerator2
        )
    );
    var providedTags = List.of(ExampleFixtureTags.EXAMPLE_ANOTHER_TAG);
    var actualResults = genericGenerator.generate(
        providedTags,
        List.of(),
        Set.of(),
        0,
        FixtureCommandsGenerator.PRIORITY_STRUCTURES
    ).toList();
    this.thenObjectApprovedWithShownIds(actualResults);
  }
}
