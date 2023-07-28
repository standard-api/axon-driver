package ai.stapi.formapi.formmapper;

import ai.stapi.formapi.FormRequest;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionData;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionElementDefinition;
import ai.stapi.test.domain.DomainTestCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class FormMapperTest extends DomainTestCase {
    
    @Autowired
    private FormMapper formMapper;
    
    @Test
    void itCanLoadFormDataForUpdateOperation() {
        var resourceId = "ExampleStructure";
        var data = new ObjectMapper().convertValue(
            new RawStructureDefinitionData(
                resourceId,
                "http://some.url/ExampleStructure",
                "draft",
                "Example Description",
                "complex-type",
                false,
                resourceId,
                null,
                new RawStructureDefinitionData.Differential(
                    new ArrayList<>(List.of(
                        new RawStructureDefinitionElementDefinition(
                            "ExampleStructure.exampleField",
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
                "CreateStructureDefinition",
                data
            )
        );
        var actual = this.formMapper.map(
            "UpdateStructureDefinition",
            new FormRequest(resourceId, null, null)
        );
        this.thenObjectApproved(actual);
    }

}