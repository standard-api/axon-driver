package ai.stapi.axonsystemplugin.aggregatedefinition;

import ai.stapi.axonsystemplugin.structuredefinition.configure.StructureDefinitionConfigured;
import ai.stapi.graphsystem.aggregatedefinition.model.ResourceAggregateDefinitionMapper;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structureSchema.ResourceStructureType;
import ai.stapi.schema.structureSchema.exception.FieldsNotFoundException;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;

public class CreateAggregateDefinitionPolicy {

  private final CommandGateway commandGateway;
  private final StructureSchemaFinder structureSchemaFinder;
  private final ResourceAggregateDefinitionMapper aggregateDefinitionMapper;
  private final ObjectMapper objectMapper;

  public CreateAggregateDefinitionPolicy(
      CommandGateway commandGateway,
      StructureSchemaFinder structureSchemaFinder,
      ResourceAggregateDefinitionMapper aggregateDefinitionMapper,
      ObjectMapper objectMapper
  ) {
    this.commandGateway = commandGateway;
    this.structureSchemaFinder = structureSchemaFinder;
    this.aggregateDefinitionMapper = aggregateDefinitionMapper;
    this.objectMapper = objectMapper;
  }

  @EventHandler
  public void on(StructureDefinitionConfigured event) {
    if (!event.getKind().equals("resource")) {
      return;
    }
    this.createAggregate(event.getStructureDefinitionId());
  }

  private void createAggregate(StructureDefinitionId structureId) {
    var id = structureId.getId();
    ResourceStructureType structureType;
    try {
      structureType = (ResourceStructureType) this.structureSchemaFinder.getStructureType(id);
    } catch (FieldsNotFoundException e) {
      throw new CannotCreateAggregateDefinition(
          String.format(
              "Cannot create automatic Aggregate Definition for '%s' resource. "
                  + "Because there was no corresponding Structure Schema to be found.",
              structureId
          ),
          e
      );
    }
    var aggregateDefinition = this.aggregateDefinitionMapper.map(structureType);
    this.commandGateway.send(
        new DynamicCommand(
            new UniqueIdentifier(aggregateDefinition.getId()),
            "CreateAggregateDefinition",
            this.objectMapper.convertValue(
                aggregateDefinition,
                new TypeReference<>() {
                }
            )
        )
    );
  }

  private static class CannotCreateAggregateDefinition extends RuntimeException {

    public CannotCreateAggregateDefinition(String message) {
      super(message);
    }

    public CannotCreateAggregateDefinition(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
