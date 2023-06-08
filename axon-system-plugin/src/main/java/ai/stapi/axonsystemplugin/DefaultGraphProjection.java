package ai.stapi.axonsystemplugin;

import ai.stapi.graph.EdgeRepository;
import ai.stapi.graph.NodeRepository;
import ai.stapi.graph.graphElementForRemoval.EdgeForRemoval;
import ai.stapi.graph.graphElementForRemoval.GraphElementForRemoval;
import ai.stapi.graph.graphElementForRemoval.NodeForRemoval;
import ai.stapi.graphoperations.synchronization.GraphSynchronizer;
import ai.stapi.graphsystem.messaging.event.AggregateGraphUpdatedEvent;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graphsystem.messaging.event.GraphUpdatedEvent;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.concurrent.atomic.AtomicInteger;
import org.axonframework.eventhandling.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultGraphProjection {

    private static final AtomicInteger lastEventIteration = new AtomicInteger(0);
    private final NodeRepository nodeRepository;
    private final EdgeRepository edgeRepository;
    private final GraphSynchronizer graphSynchronizer;
    private final Logger logger;
    private Temporal lastEventTime;

    @Autowired
    public DefaultGraphProjection(
        NodeRepository nodeRepository,
        EdgeRepository edgeRepository,
        GraphSynchronizer graphSynchronizer
    ) {
        this.nodeRepository = nodeRepository;
        this.edgeRepository = edgeRepository;
        this.graphSynchronizer = graphSynchronizer;
        this.logger = LoggerFactory.getLogger(DefaultGraphProjection.class);
        this.lastEventTime = LocalDateTime.now();
    }

    @EventHandler
    public void handle(GraphUpdatedEvent event) {
        event.getGraphElementsForRemoval().forEach(this::removeGraphElement);
        this.graphSynchronizer.synchronize(event.getSynchronizedGraph());
        this.lastEventTime = LocalDateTime.now();
//    var lastIteration = this.lastEventIteration.incrementAndGet();
        String eventName;
        if (event instanceof DynamicGraphUpdatedEvent dynamicGraphUpdatedEvent) {
            eventName = dynamicGraphUpdatedEvent.getEventName();
        } else {
            eventName = event.getClass().getSimpleName();
        }
        String message;
        if (event instanceof AggregateGraphUpdatedEvent<?> aggregateEvent) {
            message = "Event [%s] of aggregate with id [%s] was synchronized.".formatted(
                eventName,
                aggregateEvent.getIdentity()
            );
        } else {
            message = "Event [%s] was synchronized.".formatted(eventName);
        }
        this.logger.info(message);
    }

    private void removeGraphElement(GraphElementForRemoval graphElementForRemoval) {
        if (graphElementForRemoval instanceof NodeForRemoval nodeForRemoval) {
            nodeRepository.removeNode(
                nodeForRemoval.getGraphElementId(),
                nodeForRemoval.getGraphElementType()
            );
        }
        if (graphElementForRemoval instanceof EdgeForRemoval edgeForRemoval) {
            edgeRepository.removeEdge(
                edgeForRemoval.getGraphElementId(),
                edgeForRemoval.getGraphElementType()
            );
        }
    }

    public Temporal getLastEventTime() {
        return lastEventTime;
    }

    public Integer getLastEventIteration() {
        return lastEventIteration.get();
    }
}
