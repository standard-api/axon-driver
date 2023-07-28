package ai.stapi.formapi;

import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.graphsystem.systemfixtures.model.SystemModelDefinitionsLoader;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@StructureDefinitionScope(SystemModelDefinitionsLoader.SCOPE)
class FormEndpointTest extends DomainTestCase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void itShouldRespondWithJsonSchema() throws Exception {
        var result = this.mockMvc.perform(MockMvcRequestBuilders.post("/form/CreateStructureDefinition"))
            .andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        var stringResponse = result.getResponse().getContentAsString();
        var mapResponse = new ObjectMapper().readValue(stringResponse, new TypeReference<Map<String, Object>>() {
        });
        this.thenObjectApproved(mapResponse);
    }


    @Test
    public void itShouldNotOmitExtensionsWhenOmitExtensionsIsExplicitlyFalse() throws Exception {
        var result = this.mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/form/CreateStructureDefinition")
                    .content("{\"omitExtension\": \"false\"}")
                    .contentType("application/json")
            ).andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        var stringResponse = result.getResponse().getContentAsString();
        var mapResponse = new ObjectMapper().readValue(
            stringResponse,
            new TypeReference<Map<String, Object>>() {
            }
        );
        this.thenObjectApproved(mapResponse);
    }

    @Test
    public void itShouldRespondWithJsonSchemaWithTargetIdentifier() throws Exception {
        var resourceId = "SomeId";
        var eventFactoryId = "EventFactoryId";
        this.givenCommandIsDispatched(
            new DynamicCommand(
                new UniqueIdentifier(resourceId),
                "CreateAggregateDefinition",
                Map.of(
                    "id", resourceId,
                    "name", "Example Aggregate",
                    "description", "Example Aggregate Description",
                    "structure", Map.of("id", "SomeResource"),
                    "command", List.of(
                        Map.of(
                            "creationalPolicy", "always",
                            "operation", Map.of("id", "SomeOperation"),
                            "eventFactory", List.of(
                                Map.of(
                                    "id", eventFactoryId,
                                    "event", Map.of("id", "SomeEventId")
                                )
                            )
                        )
                    )
                )
            )
        );
        var result = this.mockMvc.perform(
                MockMvcRequestBuilders
                    .post("/form/AddModificationOnAggregateDefinitionCommandEventFactory")
                    .content(
                        """
                            {
                                "resourceId": "%s",
                                "targets": {
                                    "eventFactoryId": "CommandHandlerDefinitionEventFactory/%s"
                                }
                            }
                        """.formatted(resourceId, eventFactoryId)
                    ).contentType("application/json")
            ).andDo(print())
            .andExpect(status().isOk())
            .andReturn();

        var stringResponse = result.getResponse().getContentAsString();
        var mapResponse = new ObjectMapper().readValue(
            stringResponse,
            new TypeReference<Map<String, Object>>() {
            }
        );
        this.thenObjectApproved(mapResponse);
    }

}