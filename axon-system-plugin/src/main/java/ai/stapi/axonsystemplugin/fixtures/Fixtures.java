package ai.stapi.axonsystemplugin.fixtures;

import ai.stapi.graphsystem.fixtures.fixtureCommandsGenerator.FixtureCommandsGeneratorResult;
import ai.stapi.graphsystem.fixtures.fixtureCommandsGenerator.GenericFixtureCommandsGenerator;
import ai.stapi.schema.structureSchemaMapper.UnresolvableType;
import ai.stapi.schema.structureSchemaProvider.DefaultStructureSchemaProvider;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.modelling.command.AggregateVersion;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Aggregate
public class Fixtures {

  public static final String TARGET_AGGREGATE_IDENTIFIER = "Fixtures";

  @AggregateIdentifier
  private final String targetAggregateIdentifier = TARGET_AGGREGATE_IDENTIFIER;
  private final List<String> usedGenerators = new ArrayList<>();
  private final Set<String> processedFileNames = new HashSet<>();
  private final Logger logger = LoggerFactory.getLogger(Fixtures.class);

  @AggregateVersion
  private long version;

  public Fixtures() {
  }

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  public void handle(
      GenerateFixtures command,
      @Autowired GenericFixtureCommandsGenerator generator,
      @Autowired FixtureCommandsApplier applier,
      @Autowired DefaultStructureSchemaProvider provider
  ) {

    this.generateStructuresWithPriority(
        command,
        generator,
        applier,
        logger,
        command.getMinPriority(),
        command.getMaxPriority()
    );
    if (!provider.getCurrentFailedTypes().isEmpty()) {
      var missingDependencies = provider.getCurrentFailedTypes().stream()
          .map(UnresolvableType::missingDependencies)
          .flatMap(List::stream)
          .toList()
          .stream()
          .distinct()
          .toList();
      var failedTypes = provider.getCurrentFailedTypes().stream()
          .map(UnresolvableType::structureDefinitionData)
          .map(StructureDefinitionData::getId)
          .distinct()
          .toList();

      this.logger.warn(
          "Missing dependencies in DefaultStructureSchemaProvider: [" + System.lineSeparator()
              + StringUtils.join(missingDependencies, "," + System.lineSeparator())
              + System.lineSeparator() + "]"
      );
      this.logger.warn(
          "Remaining unresolved types in DefaultStructureSchemaProvider: [" + System.lineSeparator()
              + StringUtils.join(failedTypes, "," + System.lineSeparator())
              + System.lineSeparator() + "]"
      );
    }
  }

  private void generateStructuresWithPriority(
      GenerateFixtures command,
      GenericFixtureCommandsGenerator generator,
      FixtureCommandsApplier applier,
      Logger logger,
      float minPriority,
      float maxPriority
  ) {
    var results = generator.generate(
        command.getFixtureTags(),
        this.usedGenerators,
        this.processedFileNames,
        minPriority,
        maxPriority
    );
    var resultsWhichHaventBeenExecuted = results.filter(
        result -> !this.usedGenerators.contains(result.getGeneratorClassName())
            && result.getCommandDefinitions().size() > 0
    ).toList();
    var count = resultsWhichHaventBeenExecuted.stream().count();

    logger.info(String.format("Found %s fixture generators which havent been executed.", count));

    resultsWhichHaventBeenExecuted.forEach(result -> this.processResult(result, applier));
  }

  @EventSourcingHandler
  public void on(FixtureGenerated event) {
    if (event.isOneTimeGenerator()) {
      this.usedGenerators.add(event.getGeneratorClassName());
    }

    this.processedFileNames.addAll(event.getFixtureFileNames());
  }

  private void processResult(
      FixtureCommandsGeneratorResult result,
      FixtureCommandsApplier fixtureApplier
  ) {
    fixtureApplier.apply(result);
    var event = new FixtureGenerated(
        result.getGeneratorClassName(),
        result.getProcessedFiles(),
        result.isOneTimeGenerator()
    );
    AggregateLifecycle.apply(event);
  }

}
