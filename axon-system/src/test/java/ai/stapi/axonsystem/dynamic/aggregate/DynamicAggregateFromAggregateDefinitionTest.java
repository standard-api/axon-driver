package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.axonsystem.dynamic.aggregate.config.DynamicAggregateFromDefinitionTestConfiguration;
import ai.stapi.axonsystem.dynamic.aggregate.testImplementations.TestAggregateDefinitionProvider;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.test.DomainTestCase;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(DynamicAggregateFromDefinitionTestConfiguration.class)
class DynamicAggregateFromAggregateDefinitionTest extends DomainTestCase {

  @Test
  void itCanCreateDynamicAggregate() {
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleValueSetId"),
            TestAggregateDefinitionProvider.CREATE_VALUE_SET_COMMAND,
            Map.of(
                "name", "ExampleValueSet"
            )
        )
    );
    var event = this.getLastEventOfType(DynamicGraphUpdatedEvent.class);
    Assertions.assertEquals(
        TestAggregateDefinitionProvider.VALUE_SET_CREATED_EVENT,
        event.getEventName()
    );
    this.thenLastEventOfTypeGraphApproved(DynamicGraphUpdatedEvent.class);

  }

  @Test
  void itCanChangeDynamicAggregate() {
    this.givenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleValueSetId"),
            TestAggregateDefinitionProvider.CREATE_VALUE_SET_COMMAND,
            Map.of(
                "name", "ExampleValueSet"
            )
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleValueSetId"),
            TestAggregateDefinitionProvider.CHANGE_VALUE_SET_COMMAND,
            Map.of(
                "name", "ExampleChangedValueSet"
            )
        )
    );
    this.thenMergedGraphOfAggregateApproved("ExampleValueSetId");
  }

  @Test
  void itCanCreateTwoAggregatesOfSameType() {
    this.givenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleValueSetId"),
            TestAggregateDefinitionProvider.CREATE_VALUE_SET_COMMAND,
            Map.of(
                "name", "ExampleValueSet"
            )
        )
    );
    this.givenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleValueSetId2"),
            TestAggregateDefinitionProvider.CREATE_VALUE_SET_COMMAND,
            Map.of(
                "name", "ExampleValueSet2"
            )
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleValueSetId"),
            TestAggregateDefinitionProvider.CHANGE_VALUE_SET_COMMAND,
            Map.of(
                "name", "ExampleChangedValueSet"
            )
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleValueSetId2"),
            TestAggregateDefinitionProvider.CHANGE_VALUE_SET_COMMAND,
            Map.of(
                "name", "ExampleChangedValueSet2"
            )
        )
    );
    this.thenMergedGraphOfAggregateApproved("ExampleValueSetId");
  }

  @Test
  void itCanCreateDynamicAggregateIfMissing() {
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleValueSetId"),
            TestAggregateDefinitionProvider.CREATE_IF_MISSING_VALUE_SET_COMMAND,
            Map.of(
                "name", "ExampleCreatedValueSet"
            )
        )
    );
    this.thenLastEventOfTypeGraphApproved(DynamicGraphUpdatedEvent.class, 1);
  }
}