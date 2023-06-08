package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.axonsystem.dynamic.aggregate.config.DynamicAggregateFromConfigurationTestConfiguration;
import ai.stapi.axonsystem.dynamic.aggregate.testImplementations.ExampleDynamicAggregateCreatedFactory.ExampleDynamicAggregateCreated;
import ai.stapi.axonsystem.dynamic.aggregate.testImplementations.ExampleDynamicAggregateTypeConstructorCommandOgmProvider;
import ai.stapi.axonsystem.dynamic.aggregate.testImplementations.ExampleDynamicAggregateTypeCreateIfMissingCommandOgmProvider;
import ai.stapi.axonsystem.dynamic.aggregate.testImplementations.ExampleDynamicAggregateTypeMethodCommandOgmProvider;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.test.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

@Import(DynamicAggregateFromConfigurationTestConfiguration.class)
@StructureDefinitionScope(DynamicAggregateDefinitionsLoader.SCOPE)
class DynamicAggregateFromConfigurationTest extends DomainTestCase {

  @Test
  void itCanCreateDynamicAggregate() {
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId"),
            ExampleDynamicAggregateTypeConstructorCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleConstructorAttribute", "Example Constructor Attribute Value"
            )
        )
    );
    this.thenLastEventOfTypeGraphApproved(
        ExampleDynamicAggregateCreated.class);

  }

  @Test
  void itCanCreateDynamicAggregateIfMissing() {
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId"),
            ExampleDynamicAggregateTypeCreateIfMissingCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleCreateIfMissingAttribute", "Example If Missing Attribute Value"
            )
        )
    );
    this.thenLastEventOfTypeGraphApproved(
        ExampleDynamicAggregateCreated.class);

  }

  @Test
  void itCanChangeDynamicAggregateByIfMissingIfAlreadyThere() {
    this.givenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId"),
            ExampleDynamicAggregateTypeCreateIfMissingCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleCreateIfMissingAttribute", "Example If Missing Attribute Value"
            )
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId"),
            ExampleDynamicAggregateTypeCreateIfMissingCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleCreateIfMissingAttribute", "Example Changed If Missing Attribute Value"
            )
        )
    );
    this.thenLastEventOfTypeGraphApproved(
        ExampleDynamicAggregateCreated.class,
        2
    );
  }

  @Test
  void itCanChangeDynamicAggregate() {
    this.givenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId"),
            ExampleDynamicAggregateTypeConstructorCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleConstructorAttribute", "Example Constructor Attribute Value"
            )
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId"),
            ExampleDynamicAggregateTypeMethodCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleMethodAttribute", "Example Method Attribute Value"
            )
        )
    );
    this.thenMergedGraphOfAggregateApproved("ExampleId");
  }

  @Test
  void itCanCreateTwoAggregatesOfSameType() {
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId"),
            ExampleDynamicAggregateTypeConstructorCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleConstructorAttribute", "Example Constructor Attribute Value"
            )
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId2"),
            ExampleDynamicAggregateTypeConstructorCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleConstructorAttribute", "Example Constructor Attribute Value 2"
            )
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId"),
            ExampleDynamicAggregateTypeMethodCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleMethodAttribute", "Example Method Attribute Value"
            )
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("ExampleId2"),
            ExampleDynamicAggregateTypeMethodCommandOgmProvider.COMMAND_NAME,
            Map.of(
                "exampleMethodAttribute", "Example Method Attribute Value 2"
            )
        )
    );
    this.thenMergedGraphOfAggregateApproved("ExampleId");
  }
}