package ai.stapi.formapi.formmapper;

import ai.stapi.formapi.formmapper.exceptions.CannotLoadFormData;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionDTO;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.ImportStructureDefinition;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionData;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionElementDefinition;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import ai.stapi.test.domain.DomainTestCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class AggregateRepositoryFormDataLoaderTest extends DomainTestCase {
    
    @Autowired
    private AggregateRepositoryFormDataLoader aggregateRepositoryFormDataLoader;
    
    @Autowired
    private OperationDefinitionProvider operationDefinitionProvider;
    
    @Test
    void itThrowsExceptionWhenCommandHandlerDefinitionForProvidedOperationDoesntExist() {
        Executable throwable = () -> this.aggregateRepositoryFormDataLoader.load(
            new OperationDefinitionDTO(
                "NotExisting",
                "NotExisting",
                "Irrelevant",
                "Irrelevant",
                "Irrelevant",
                "Irrelevant",
                List.of(),
                false,
                false,
                false,
                List.of()
            ),
            "Irrelevant",
            Map.of()
        );
        this.thenExceptionMessageApproved(CannotLoadFormData.class, throwable);
    }

    @Test
    void itThrowsExceptionWhenAggregateWithProvidedResourceIdDoesntExist() {
        var operationId = "CreateParameters";
        var resourceId = "NonExistingId";
        var operation = this.operationDefinitionProvider.provide(operationId);
        Executable throwable = () -> this.aggregateRepositoryFormDataLoader.load(
            operation,
            resourceId,
            Map.of()
        );
        this.thenExceptionMessageApproved(CannotLoadFormData.class, throwable);
    }

    @Test
    void itCanLoadFormDataForSimpleOperation() {
        var operationId = "CreateParameters";
        var resourceId = "SomeId";
        this.givenCommandIsDispatched(
            new DynamicCommand(
                new UniqueIdentifier(resourceId),
                operationId,
                Map.of(
                    "language", "cz",
                    "implicitRules", "http://some.uri.com"
                )
            )
        );
        var operation = this.operationDefinitionProvider.provide(operationId);
        var actual = this.aggregateRepositoryFormDataLoader.load(
            operation,
            resourceId,
            Map.of()
        );
        this.thenObjectApproved(actual);
    }

    @Test
    void itCanLoadFormDataForComplexCommand() {
        var operationId = "CreateStructureDefinition";
        var resourceId = "SomeId";
        var data = new ObjectMapper().convertValue(
            new RawStructureDefinitionData(
                resourceId,
                "http://some.url/SomeId",
                "draft",
                "Example Description",
                "complex-type",
                false,
                resourceId,
                null,
                new RawStructureDefinitionData.Differential(
                    new ArrayList<>(List.of(
                        new RawStructureDefinitionElementDefinition(
                            "ExampleComplex.exampleField",
                            1,
                            "1",
                            "Example field description",
                            "Example field description",
                            "Example field description",
                            new ArrayList<>(List.of(
                                new RawStructureDefinitionElementDefinition.ElementDefinitionType(
                                    "string",
                                    new ArrayList<>()
                                )
                            ))
                        )
                    ))
                )
            ),
            new TypeReference<Map<String, Object>>() {
            }
        );
        this.givenCommandIsDispatched(
            new DynamicCommand(
                new UniqueIdentifier(resourceId),
                operationId,
                data
            )
        );
        var operation = this.operationDefinitionProvider.provide(operationId);
        var actual = this.aggregateRepositoryFormDataLoader.load(
            operation,
            resourceId,
            Map.of()
        );
        this.thenObjectApproved(actual);
    }
}