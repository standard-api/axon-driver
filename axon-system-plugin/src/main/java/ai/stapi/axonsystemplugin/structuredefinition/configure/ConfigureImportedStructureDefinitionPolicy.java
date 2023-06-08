package ai.stapi.axonsystemplugin.structuredefinition.configure;

import ai.stapi.axonsystem.dynamic.event.DynamicEventHandler;
import ai.stapi.graph.traversableGraphElements.TraversableEdge;
import ai.stapi.graph.traversableGraphElements.TraversableNode;
import ai.stapi.graphoperations.graphDeserializers.ogmDeserializer.GenericGraphToObjectDeserializer;
import ai.stapi.graphoperations.graphDeserializers.ogmDeserializer.MissingTraversalTargetResolvingStrategy;
import ai.stapi.graphsystem.messaging.event.AggregateGraphUpdatedEvent;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.StructureDefinitionImported;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.ElementDefinitionType;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import java.util.List;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.eventhandling.EventHandler;
import org.jetbrains.annotations.Nullable;


public class ConfigureImportedStructureDefinitionPolicy {

  private final GenericGraphToObjectDeserializer genericGraphToObjectDeserializer;
  private final CommandGateway commandGateway;


  public ConfigureImportedStructureDefinitionPolicy(
      GenericGraphToObjectDeserializer genericGraphToObjectDeserializer,
      CommandGateway commandGateway
  ) {
    this.genericGraphToObjectDeserializer = genericGraphToObjectDeserializer;
    this.commandGateway = commandGateway;
  }

  @EventHandler
  public void on(StructureDefinitionImported event) {
    this.configureStructureDefinition(event);
  }

  @DynamicEventHandler(messageName = "StructureDefinitionCreated")
  public void on(DynamicGraphUpdatedEvent event) {
    this.configureStructureDefinition(event);
  }

  private void configureStructureDefinition(
      AggregateGraphUpdatedEvent<? extends UniqueIdentifier> event
  ) {
    var inMemoryGraphRepository = event.getSynchronizedGraph().traversable();
    var traversableNode = inMemoryGraphRepository.loadNode(event.getIdentity());
    var structureDefinitionDTO = this.genericGraphToObjectDeserializer.deserialize(
        traversableNode,
        StructureDefinitionData.class,
        inMemoryGraphRepository,
        MissingTraversalTargetResolvingStrategy.LENIENT
    );
    this.commandGateway.send(
        new ConfigureStructureDefinition(structureDefinitionDTO)
    );
  }

  @DynamicEventHandler(messageName = "ElementOnStructureDefinitionDifferentialAdded")
  public void onElementAdded(DynamicGraphUpdatedEvent event) {
    var structureId = event.getIdentity().getId();
    var graph = event.getSynchronizedGraph().traversable();
    var differentials = graph.loadAllNodes(
        "StructureDefinitionDifferential"
    );
    if (differentials.size() != 1) {
      throw new RuntimeException("This should never happen.");
    }
    var differential = differentials.get(0);
    var elements = differential.getEdges("element")
        .stream()
        .map(TraversableEdge::getNodeTo)
        .map(node -> new ElementDefinition(
            this.getAttributeOrNull(node, "path", String.class),
            node.getEdges("type").stream()
                .map(TraversableEdge::getNodeTo)
                .map(typeNode -> new ElementDefinitionType(
                    this.getAttributeOrNull(typeNode, "code", String.class),
                    (List<String>) this.getAttributeOrNull(typeNode, "targetProfile", List.class)
                ))
                .toList(),
            this.getAttributeOrNull(node, "min", Integer.class),
            this.getAttributeOrNull(node, "max", String.class),
            this.getAttributeOrNull(node, "shortDescription", String.class),
            this.getAttributeOrNull(node, "definition", String.class),
            this.getAttributeOrNull(node, "comment", String.class)
        ))
        .toList();

    this.commandGateway.send(
        new ConfigureElementsToStructureDefinition(
            new StructureDefinitionId(structureId),
            elements
        )
    );
  }

  @Nullable
  private <T> T getAttributeOrNull(
      TraversableNode node,
      String attributeName,
      Class<T> clazz
  ) {
    if (!node.hasAttribute(attributeName)) {
      return null;
    }
    return (T) node.getAttribute(attributeName).getValue();
  }
}
