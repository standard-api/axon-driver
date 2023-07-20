package ai.stapi.axonsystemplugin.aggregatedefinition.createCRUDCommandHandlers;

import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@StructureDefinitionScope(CreateCrudCommandHandlerPolicyTestDefinitionsLoader.SCOPE)
@ActiveProfiles("dev")
class CreateCRUDCommandHandlerPolicyTest extends DomainTestCase {

  @Test
  void itCanCreateBasicCrudOperationWhenAggregateDefinitionAdded() {
    var expectedAggregateIdentifier = new UniqueIdentifier("ExampleTestResourceAggregate");
    this.whenCommandIsDispatched(
        new DynamicCommand(
            expectedAggregateIdentifier,
            "CreateAggregateDefinition",
            Map.of(
                "id", "ExampleTestResourceAggregate",
                "name", "ExampleTestResource",
                "description", "This is a example test aggregate.",
                "tag", new ArrayList<>(List.of("tag1", "tag2", "tag3")),
                "structure", new HashMap<>(Map.of(
                    "id", "ExampleTestResource"
                ))
            )
        )
    );
    this.thenExpectedDynamicEventsSaved(
        "AggregateDefinitionCreated",
        "OperationDefinitionCreated",
        "EventMessageDefinitionCreated",
        "CommandOnAggregateDefinitionAdded",
        "OperationDefinitionCreated",
        "EventMessageDefinitionCreated",
        "CommandOnAggregateDefinitionAdded",
        "OperationDefinitionCreated",
        "EventMessageDefinitionCreated",
        "CommandOnAggregateDefinitionAdded"
    );
    this.thenMergedGraphOfAggregateApproved(expectedAggregateIdentifier);
  }
}
