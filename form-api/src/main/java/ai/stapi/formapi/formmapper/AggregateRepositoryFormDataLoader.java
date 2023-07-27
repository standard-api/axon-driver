package ai.stapi.formapi.formmapper;

import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregate;
import ai.stapi.axonsystem.graphaggregate.AggregateWithGraph;
import ai.stapi.formapi.formmapper.exceptions.CannotLoadFormData;
import ai.stapi.graphoperations.graphLanguage.graphDescription.graphDescriptionBuilder.GraphDescriptionBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.NodeDescriptionParameters;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.query.NodeQueryGraphDescription;
import ai.stapi.graphoperations.graphLoader.inmemory.InMemoryGraphLoaderProvider;
import ai.stapi.graphoperations.graphLoader.search.SearchQueryParameters;
import ai.stapi.graphoperations.objectGraphLanguage.objectGraphMappingBuilder.GenericOGMBuilder;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionDTO;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.model.exceptions.CannotProvideAggregateDefinition;
import ai.stapi.graphsystem.aggregategraphstatemodifier.EventFactoryModificationTraverser;
import ai.stapi.graphsystem.aggregategraphstatemodifier.EventModificatorOgmProvider;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionStructureTypeMapper;
import ai.stapi.identity.UniqueIdentifier;
import org.axonframework.commandhandling.GenericCommandMessage;
import org.axonframework.config.Configuration;
import org.axonframework.messaging.unitofwork.CurrentUnitOfWork;
import org.axonframework.messaging.unitofwork.DefaultUnitOfWork;
import org.axonframework.modelling.command.Aggregate;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.modelling.command.Repository;

import java.util.HashMap;
import java.util.Map;

public class AggregateRepositoryFormDataLoader implements FormDataLoader {

    private final Repository<DynamicAggregate> repository;
    private final AggregateDefinitionProvider aggregateDefinitionProvider;
    private final EventFactoryModificationTraverser eventFactoryModificationTraverser;
    private final EventModificatorOgmProvider eventModificatorOgmProvider;
    private final OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper;
    private final InMemoryGraphLoaderProvider inMemoryGraphLoaderProvider;

    public AggregateRepositoryFormDataLoader(
        Configuration configuration,
        AggregateDefinitionProvider aggregateDefinitionProvider,
        EventFactoryModificationTraverser eventFactoryModificationTraverser,
        EventModificatorOgmProvider eventModificatorOgmProvider,
        OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper,
        InMemoryGraphLoaderProvider inMemoryGraphLoaderProvider
    ) {
        this.repository = configuration.repository(DynamicAggregate.class);
        this.aggregateDefinitionProvider = aggregateDefinitionProvider;
        this.eventFactoryModificationTraverser = eventFactoryModificationTraverser;
        this.eventModificatorOgmProvider = eventModificatorOgmProvider;
        this.operationDefinitionStructureTypeMapper = operationDefinitionStructureTypeMapper;
        this.inMemoryGraphLoaderProvider = inMemoryGraphLoaderProvider;
    }

    @Override
    public Map<String, Object> load(
        OperationDefinitionDTO operationDefinitionDTO,
        String resourceId,
        Map<String, Object> possibleStartIds
    ) {
        var operationId = operationDefinitionDTO.getId();
        AggregateDefinitionDTO aggregateDefinition;
        try {
            aggregateDefinition = this.aggregateDefinitionProvider.getAggregateForOperation(operationId);
        } catch (CannotProvideAggregateDefinition e) {
            throw CannotLoadFormData.becauseThereWasNoCommandHandlerDefinitionForProvidedOperation(operationId, e);
        }
        var commandHandlerDefinition = aggregateDefinition.getCommand()
            .stream()
            .filter(command -> command.getOperation().getId().equals(operationId))
            .findFirst()
            .orElseThrow(
                () -> CannotLoadFormData.becauseThereWasNoCommandHandlerDefinitionForProvidedOperation(operationId)
            );

        var unitOfWork = DefaultUnitOfWork.startAndGet(
            new GenericCommandMessage<>(String.format(
                "Loading state of aggregate '%s' with id '%s'. To be used as form data for operation '%s'.",
                aggregateDefinition.getName(),
                resourceId,
                operationId
            ))
        );
        CurrentUnitOfWork.set(unitOfWork);
        Aggregate<DynamicAggregate> aggregate;
        try {
            aggregate = this.repository.load(resourceId);
        } catch (AggregateNotFoundException e) {
            throw CannotLoadFormData.becauseAggregateByProvidedResourceIdWasNotFound(resourceId, e);
        } finally {
            CurrentUnitOfWork.clear(unitOfWork);
        }
        var operationStructureType = this.operationDefinitionStructureTypeMapper.map(operationDefinitionDTO);

        var state = aggregate.invoke(AggregateWithGraph::getInMemoryGraphRepository);
        var loader = this.inMemoryGraphLoaderProvider.provide(state);
        var data = new HashMap<String, Object>();
        commandHandlerDefinition
            .getEventFactory()
            .stream()
            .flatMap(eventFactory -> eventFactory.getModification().stream())
            .forEach(modification -> {
                var inputValueParameterName = modification.getInputValueParameterName();
                var traversingStart = this.eventFactoryModificationTraverser.getTraversingStartNode(
                    aggregateDefinition.getStructure().getId(),
                    new UniqueIdentifier(resourceId),
                    possibleStartIds,
                    modification,
                    operationStructureType,
                    state
                );
                var modificationPath = modification.getModificationPath();
                var splitPath = modificationPath.split("\\.");
                var modifiedNode = this.eventFactoryModificationTraverser.traverseToModifiedNode(
                    traversingStart,
                    splitPath,
                    operationStructureType,
                    modification
                );
                var inputValueSchema = operationStructureType.getField(inputValueParameterName);
                var fieldName = splitPath[splitPath.length - 1];
                var ogm = this.eventModificatorOgmProvider.getOgm(
                    modifiedNode,
                    inputValueSchema,
                    fieldName
                );
                var graphDescription = GenericOGMBuilder.getCompositeGraphDescriptionFromOgm(ogm);
                var filtered = new GraphDescriptionBuilder().filterOutNullDescriptions(graphDescription);
                var nodeQueryDescription = new NodeQueryGraphDescription(
                    (NodeDescriptionParameters) filtered.getParameters(),
                    SearchQueryParameters.from(),
                    filtered.getChildGraphDescriptions()
                );
                var output = loader.get(
                    modifiedNode.getId(),
                    nodeQueryDescription,
                    Map.class
                );
                var value = output.getData().get(fieldName);
                data.put(inputValueParameterName, value);
            });

        return data;
    }
}
