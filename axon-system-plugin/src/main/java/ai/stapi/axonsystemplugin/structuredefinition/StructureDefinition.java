package ai.stapi.axonsystemplugin.structuredefinition;

import ai.stapi.axonsystem.graphaggregate.AggregateWithDynamicGraph;
import ai.stapi.graphoperations.objectGraphMapper.model.MissingFieldResolvingStrategy;
import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.FixedImportStructureDefinition;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.ImportStructureDefinition;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.StructureDefinitionImported;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import ai.stapi.schema.structuredefinition.StructureDefinitionNormalizer;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;

@Aggregate
public class StructureDefinition extends AggregateWithDynamicGraph<StructureDefinitionId> {

  @CommandHandler
  @CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
  public void handle(
      ImportStructureDefinition command,
      @Autowired DynamicCommandProcessor commandProcessor
  ) {
    this.commandProcessor = commandProcessor;
    this.processCommandDynamically(
        new FixedImportStructureDefinition(
            command.getTargetIdentifier(),
            StructureDefinitionNormalizer.normalize(command.getStructureDefinitionSource())
        ),
        MissingFieldResolvingStrategy.LENIENT
    );
  }

  @EventSourcingHandler
  public void on(StructureDefinitionImported event) {
    this.onAggregateCreated(event);
  }
}