package ai.stapi.formapi.formmapper;

import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregate;
import ai.stapi.axonsystem.graphaggregate.AggregateWithGraph;
import ai.stapi.formapi.formmapper.exceptions.CannotLoadFormData;
import ai.stapi.graphoperations.dagtoobjectconverter.DAGToObjectConverter;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionDTO;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.model.exceptions.CannotProvideAggregateDefinition;
import ai.stapi.graphsystem.aggregategraphstatemodifier.EventFactoryModificationTraverser;
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
    private final OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper;

    private final DAGToObjectConverter dagToObjectConverter;

    public AggregateRepositoryFormDataLoader(
        Configuration configuration,
        AggregateDefinitionProvider aggregateDefinitionProvider,
        EventFactoryModificationTraverser eventFactoryModificationTraverser,
        OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper,
        DAGToObjectConverter dagToObjectConverter
    ) {
        this.repository = configuration.repository(DynamicAggregate.class);
        this.aggregateDefinitionProvider = aggregateDefinitionProvider;
        this.eventFactoryModificationTraverser = eventFactoryModificationTraverser;
        this.operationDefinitionStructureTypeMapper = operationDefinitionStructureTypeMapper;
        this.dagToObjectConverter = dagToObjectConverter;
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
                var fieldName = splitPath[splitPath.length - 1];
                var object = this.dagToObjectConverter.convert(modifiedNode);
                var value = object.get(fieldName);
                if (value != null) {
                    data.put(inputValueParameterName, value);
                }

            });

        return data;
    }
}
