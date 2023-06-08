package ai.stapi.axonsystemplugin.fixtures;

import ai.stapi.axonsystemplugin.exampleimplmentation.ExampleNodeCreated;
import ai.stapi.axonsystemplugin.exampleimplmentation.configuration.ExampleModuleDefinitionsLoader;
import ai.stapi.axonsystemplugin.fixtures.ExampleImplementations.ExampleFixtureCommandsGenerator;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(ExampleModuleDefinitionsLoader.SCOPE)
class FixtureCommandsApplierTest extends DomainTestCase {

  @Autowired
  private ExampleFixtureCommandsGenerator generator;

  @Autowired
  private FixtureCommandsApplier applier;

  @Test
  void itShouldApplyGeneratedExampleCommands() {
    this.applier.apply(this.generator.generate(Set.of()));
    this.thenExpectedEventTypesSaved(ExampleNodeCreated.class);
  }
}