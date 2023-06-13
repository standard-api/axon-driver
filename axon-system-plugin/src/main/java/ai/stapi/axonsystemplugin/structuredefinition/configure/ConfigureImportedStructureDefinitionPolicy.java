package ai.stapi.axonsystemplugin.structuredefinition.configure;

import ai.stapi.axonsystem.dynamic.event.DynamicEventHandler;
import ai.stapi.graph.traversableGraphElements.TraversableEdge;
import ai.stapi.graph.traversableGraphElements.TraversableNode;
import ai.stapi.graphoperations.graphLoader.GraphLoaderReturnType;
import ai.stapi.graphoperations.graphLoader.inmemory.InMemoryGraphLoaderProvider;
import ai.stapi.graphsystem.messaging.event.AggregateGraphUpdatedEvent;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.StructureDefinitionImported;
import ai.stapi.graphsystem.structuredefinition.loader.DatabaseStructureDefinitionLoader;
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

  private final CommandGateway commandGateway;
  private final InMemoryGraphLoaderProvider inMemoryGraphLoaderProvider;

  public ConfigureImportedStructureDefinitionPolicy(
      CommandGateway commandGateway,
      InMemoryGraphLoaderProvider inMemoryGraphLoaderProvider
  ) {
    this.commandGateway = commandGateway;
    this.inMemoryGraphLoaderProvider = inMemoryGraphLoaderProvider;
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
    var loader = this.inMemoryGraphLoaderProvider.provide(event.getSynchronizedGraph());
    var output = loader.get(
        event.getIdentity(),
        DatabaseStructureDefinitionLoader.STRUCTURE_DEFINITION_GRAPH_DESCRIPTION,
        StructureDefinitionData.class,
        GraphLoaderReturnType.OBJECT
    );
    this.commandGateway.send(
        new ConfigureStructureDefinition(output.getData())
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
