package ai.stapi.axonsystemplugin.fixtures;

import ai.stapi.axonsystemplugin.exampleimplmentation.ExampleNodeCreated;
import ai.stapi.axonsystemplugin.exampleimplmentation.ExampleNodeExampleQuantityChanged;
import ai.stapi.axonsystemplugin.exampleimplmentation.configuration.ExampleModuleDefinitionsLoader;
import ai.stapi.axonsystemplugin.fixtures.ExampleImplementations.ExampleFixtureTags;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

@StructureDefinitionScope(ExampleModuleDefinitionsLoader.SCOPE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FixturesTest extends DomainTestCase {

  @Test
  void itShouldApplyCommandsOfProvidedTag() {
    var command = new GenerateFixtures(ExampleFixtureTags.EXAMPLE_TAG);
    this.whenCommandIsDispatched(command);
    this.thenExpectedEventTypesSaved(
        ExampleNodeCreated.class,
        FixtureGenerated.class
    );
  }

  @Test
  void itShouldApplyCommandsOfAnotherProvidedTag() {
    var givenCommand = new GenerateFixtures(ExampleFixtureTags.EXAMPLE_TAG);
    this.givenCommandIsDispatched(givenCommand);
    this.thenExpectedEventTypesSaved(
        ExampleNodeCreated.class,
        FixtureGenerated.class
    );

    var command = new GenerateFixtures(ExampleFixtureTags.EXAMPLE_ANOTHER_TAG);
    this.whenCommandIsDispatched(command);
    this.thenExpectedEventTypesSaved(
        ExampleNodeCreated.class,
        FixtureGenerated.class,
        ExampleNodeCreated.class,
        FixtureGenerated.class
    );
  }

  @Test
  void itShouldNotApplyCommandsWhenProvidedTagAlreadyUsed() {
    var givenCommand = new GenerateFixtures(ExampleFixtureTags.EXAMPLE_TAG);
    this.givenCommandIsDispatched(givenCommand);

    var firstTimeCommand = new GenerateFixtures(ExampleFixtureTags.EXAMPLE_CHANGE_TAG);
    this.givenCommandIsDispatched(firstTimeCommand);

    var secondTimeCommand = new GenerateFixtures(ExampleFixtureTags.EXAMPLE_CHANGE_TAG);
    this.whenCommandIsDispatched(secondTimeCommand);

    this.thenExpectedEventTypesSaved(
        ExampleNodeCreated.class,
        FixtureGenerated.class,
        ExampleNodeExampleQuantityChanged.class,
        FixtureGenerated.class
    );
  }
}

