package ai.stapi.axonsystemplugin.aggregatedefinition.createCRUDCommandHandlers;

import ai.stapi.axonsystem.dynamic.event.DynamicEventHandler;
import ai.stapi.axonsystemplugin.structuredefinition.configure.ElementsToStructureDefinitionConfigured;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.EventFactoryModificationResult;
import ai.stapi.graphsystem.aggregatedefinition.model.eventFactory.OperationEventFactoriesMapper;
import ai.stapi.graphsystem.eventdefinition.EventMessageDefinitionData;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.OperationDefinitionParameters;
import ai.stapi.graphsystem.operationdefinition.model.resourceStructureTypeOperationsMapper.ResourceOperationsMapper;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.ResourceStructureType;
import ai.stapi.schema.structureSchema.exception.FieldsNotFoundException;
import ai.stapi.schema.structureSchemaMapper.StructureDefinitionToSSMapper;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCreateCRUDCommandHandlerPolicy {

  private final CommandGateway commandGateway;
  private final StructureSchemaFinder schemaFinder;
  private final ObjectMapper objectMapper;
  private final StructureDefinitionToSSMapper structureDefinitionToSSMapper;

  protected AbstractCreateCRUDCommandHandlerPolicy(
      CommandGateway commandGateway,
      StructureSchemaFinder schemaFinder,
      ObjectMapper objectMapper,
      StructureDefinitionToSSMapper structureDefinitionToSSMapper
  ) {
    this.commandGateway = commandGateway;
    this.schemaFinder = schemaFinder;
    this.objectMapper = objectMapper;
    this.structureDefinitionToSSMapper = structureDefinitionToSSMapper;
  }

  protected abstract ResourceOperationsMapper resourceOperationsMapper();

  protected abstract OperationEventFactoriesMapper operationEventDefinitionMapper();

  protected abstract String creationalPolicy();

  @DynamicEventHandler(messageName = "AggregateDefinitionCreated")
  public void on(DynamicGraphUpdatedEvent event) {
    var aggregateType = event.getSynchronizedGraph().traversable()
        .loadNode(event.getIdentity(), "AggregateDefinition")
        .getEdges("structure")
        .get(0)
        .getNodeTo()
        .getId()
        .getId();

    var resourceStructureType = this.getResourceStructureType(aggregateType);
    var operations = this.resourceOperationsMapper()
        .map(resourceStructureType)
        .getOperations();

    this.handleNewOperations(event.getIdentity().getId(), operations);
  }

  @EventHandler
  public void on(ElementsToStructureDefinitionConfigured event) {
    var groupedByType = new HashMap<String, List<String>>();
    event.getElementPaths().forEach(path -> {
      var split = path.split("\\.");
      if (split.length < 2) {
        throw new RuntimeException("This should never happen.");
      }
      String modifiedType;
      if (split.length == 2) {
        modifiedType = split[0];
      } else {
        modifiedType =
            this.structureDefinitionToSSMapper.createAnonymousComplexTypeNameFromElementPath(
                String.join(
                    ".",
                    Arrays.copyOfRange(split, 0, split.length - 1)
                )
            );
      }
      var fieldName = split[split.length - 1];
      groupedByType.computeIfAbsent(modifiedType, key -> new ArrayList<>()).add(fieldName);
    });
    groupedByType.forEach((type, fieldNames) -> {
      var structureType = (ComplexStructureType) this.schemaFinder.getStructureType(type);
      var result = this.resourceOperationsMapper().mapNewFields(
          structureType,
          fieldNames
      );
      result.forEach((resourceId, subResult) -> {
        this.handleNewOperations(resourceId, subResult.getOperations());
        this.handleOperationChanges(resourceId, subResult.getNewParameters());
      });
    });
  }

  private void handleNewOperations(
      String resourceId,
      List<OperationDefinitionDTO> operations
  ) {
    if (operations.isEmpty()) {
      return;
    }
    operations.stream()
        .map(this::createOperationDefinitionCommand)
        .forEach(this.commandGateway::send);

    var commandHandlers = new ArrayList<HashMap<String, Object>>();
    operations.forEach(operation -> {
      var eventFactories = this.operationEventDefinitionMapper().map(operation);
      eventFactories.stream()
          .map(eventFactory -> this.createEventDefinitionCommand(eventFactory.getEvent()))
          .forEach(this.commandGateway::send);
      var commandHandler = new HashMap<>(Map.of(
          "operation", new HashMap<>(
              Map.of(
                  "id", operation.getId()
              )
          ),
          "creationalPolicy", this.creationalPolicy(),
          "eventFactory", eventFactories.stream().map(
              eventFactory -> new HashMap<>(Map.of(
                  "id", eventFactory.getId(),
                  "event", new HashMap<>(Map.of(
                      "id", eventFactory.getEvent().getId()
                  )),
                  "modification", eventFactory.getModification().stream()
                      .map(modification -> this.objectMapper.convertValue(
                          modification,
                          HashMap.class
                      )).collect(Collectors.toList())
              )
              )).collect(Collectors.toList())
      ));
      commandHandlers.add(commandHandler);
    });
    this.commandGateway.send(
        new DynamicCommand(
            new UniqueIdentifier(resourceId),
            "AddCommandOnAggregateDefinition",
            Map.of(
                "command", commandHandlers
            )
        )
    );
  }

  private void handleOperationChanges(
      String resourceId,
      List<OperationDefinitionParameters> newParameters
  ) {
    newParameters.stream()
        .filter(params -> !params.getParameters().isEmpty())
        .map(this::createAddParameterCommand)
        .forEach(this.commandGateway::send);

    newParameters.stream()
        .filter(params -> !params.getParameters().isEmpty())
        .forEach(parameters -> {
          var modifications = this.operationEventDefinitionMapper()
              .mapParameters(parameters);

          modifications.stream()
              .map(result -> this.createAddModificationCommand(resourceId, result))
              .forEach(this.commandGateway::send);
        });
  }

  @NotNull
  private DynamicCommand createOperationDefinitionCommand(OperationDefinitionDTO operation) {
    return new DynamicCommand(
        new UniqueIdentifier(operation.getId()),
        "CreateOperationDefinition",
        this.objectMapper.convertValue(operation, new TypeReference<>() {
        })
    );
  }

  @NotNull
  private DynamicCommand createEventDefinitionCommand(EventMessageDefinitionData eventDefinition) {
    return new DynamicCommand(
        new UniqueIdentifier(eventDefinition.getId()),
        "CreateEventMessageDefinition",
        this.objectMapper.convertValue(eventDefinition, new TypeReference<>() {
        })
    );
  }

  private DynamicCommand createAddParameterCommand(
      OperationDefinitionParameters operationDefinitionParameters
  ) {
    return new DynamicCommand(
        new UniqueIdentifier(operationDefinitionParameters.getOperationId()),
        "AddParameterOnOperationDefinition",
        Map.of(
            "parameter", this.objectMapper.convertValue(
                operationDefinitionParameters.getParameters(),
                new TypeReference<List<HashMap<String, Object>>>() {
                }
            )
        )
    );
  }

  private DynamicCommand createAddModificationCommand(
      String resourceId,
      EventFactoryModificationResult result
  ) {
    return new DynamicCommand(
        new UniqueIdentifier(resourceId),
        "AddModificationOnAggregateDefinitionCommandEventFactory",
        Map.of(
            "eventFactoryId", String.format("AggregateDefinitionCommandEventFactory/%s", result.getEventFactoryId()),
            "modification", this.objectMapper.convertValue(
                result.getEventFactoryModifications(),
                new TypeReference<List<HashMap<String, Object>>>() {
                }
            )
        )
    );
  }

  private ResourceStructureType getResourceStructureType(String aggregateType) {
    try {
      return (ResourceStructureType) this.schemaFinder.getStructureType(aggregateType);
    } catch (FieldsNotFoundException e) {
      throw new CannotCreateAutomaticOperationForAggregate(
          String.format(
              "Cannot create automatic Operations for Aggregate '%s' with '%s', " +
                  "because related Structure Schema was not found.",
              aggregateType,
              this.resourceOperationsMapper().getClass().getCanonicalName()
          ),
          e
      );
    }
  }

  private static class CannotCreateAutomaticOperationForAggregate extends RuntimeException {

    public CannotCreateAutomaticOperationForAggregate(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
