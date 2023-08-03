package ai.stapi.formapi.forminfo;

import ai.stapi.formapi.forminfo.exceptions.CannotMapFormInfo;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionDTO;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO;
import ai.stapi.graphsystem.aggregatedefinition.model.exceptions.CannotProvideAggregateDefinition;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.FieldType;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FormInfoMapper {

    private final AggregateDefinitionProvider aggregateDefinitionProvider;
    private final OperationDefinitionProvider operationDefinitionProvider;
    private final StructureSchemaFinder structureSchemaFinder;

    public FormInfoMapper(
        AggregateDefinitionProvider aggregateDefinitionProvider,
        OperationDefinitionProvider operationDefinitionProvider,
        StructureSchemaFinder structureSchemaFinder
    ) {
        this.aggregateDefinitionProvider = aggregateDefinitionProvider;
        this.operationDefinitionProvider = operationDefinitionProvider;
        this.structureSchemaFinder = structureSchemaFinder;
    }

    public FormInfo map(String operationId) {
        AggregateDefinitionDTO aggregateDefinition;
        try {
            aggregateDefinition = this.aggregateDefinitionProvider.getAggregateForOperation(operationId);
        } catch (CannotProvideAggregateDefinition e) {
            throw CannotMapFormInfo.becauseThereWasNoCommandHandlerDefinitionForProvidedOperation(operationId, e);
        }
        var commandHandlerDefinition = aggregateDefinition.getCommand()
            .stream()
            .filter(command -> command.getOperation().getId().equals(operationId))
            .findFirst()
            .orElseThrow(
                () -> CannotMapFormInfo.becauseThereWasNoCommandHandlerDefinitionForProvidedOperation(operationId)
            );

        var resourceType = aggregateDefinition.getStructure().getId();
        var operation = this.operationDefinitionProvider.provide(operationId);
        var resourceStructure = (ComplexStructureType) this.structureSchemaFinder.getStructureType(resourceType);
        var result = new HashMap<String, Set<String>>();

        commandHandlerDefinition.getEventFactory()
            .stream()
            .flatMap(eventFactory -> eventFactory.getModification().stream())
            .filter(modification -> modification.getStartIdParameterName() != null)
            .map(modification -> operation.getParameter(modification.getStartIdParameterName()))
            .forEach(
                parameter -> result.put(
                    parameter.getName(),
                    this.getPossibleTypes(resourceStructure, parameter.getReferencedFrom())
                )
            );

        return new FormInfo(
            result,
            resourceType,
            Objects.equals(
                commandHandlerDefinition.getCreationalPolicy(), 
                CommandHandlerDefinitionDTO.CreationPolicy.NEVER
            )
        );
    }

    private Set<String> getPossibleTypes(
        ComplexStructureType resourceStructureType,
        List<OperationDefinitionDTO.ParameterDTO.ReferencedFrom> referencedFrom
    ) {
        return referencedFrom.stream()
            .map(OperationDefinitionDTO.ParameterDTO.ReferencedFrom::getSource)
            .map(source -> source.split("\\."))
            .map(splitSource -> Arrays.copyOfRange(splitSource,1, splitSource.length - 1))
            .flatMap(splitSource -> this.getPossibleTypes(resourceStructureType, splitSource))
            .collect(Collectors.toSet());
    }

    private Stream<String> getPossibleTypes(ComplexStructureType currentStructureType, String[] splitSource) {
        var currentField = splitSource[0];
        if (!currentStructureType.hasField(currentField)) {
            return Stream.of();
        }
        var types = currentStructureType.getField(currentField)
            .getTypes()
            .stream()
            .filter(fieldType -> !fieldType.isPrimitiveType())
            .map(FieldType::getType);
            
        if (splitSource.length == 1) {
            return types;
        }
        var restOfPath = Arrays.copyOfRange(splitSource, 1, splitSource.length);
        
        return types
            .map(this.structureSchemaFinder::getStructureType)
            .map(ComplexStructureType.class::cast)
            .flatMap(structure -> this.getPossibleTypes(structure, restOfPath));
    }
}
