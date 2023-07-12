package ai.stapi.axonsystemplugin.aggregatedefinition;

import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.ImportStructureDefinition;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionData;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionElementDefinition;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import ai.stapi.test.domain.DomainTestCase;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
class CreateAggregateDefinitionPolicyTest extends DomainTestCase {

  @Test
  void itWillNotReactToComplexStructure() {
    this.whenCommandIsDispatched(
        new ImportStructureDefinition(
            new StructureDefinitionId("ExampleComplex"),
            new RawStructureDefinitionData(
                "ExampleComplex",
                "http://some.url/ExampleComplex",
                "draft",
                "Example Description",
                "complex-type",
                false,
                "ExampleComplex",
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
            )
        )
    );
    var maybeEvent = this.getLastEventOfTypeOptionally(DynamicGraphUpdatedEvent.class);
    Assertions.assertTrue(maybeEvent.isEmpty());
  }

  @Test
  void itWillCreateAggregateDefinitionWhenResourceAdded() {
    this.whenCommandIsDispatched(
        new ImportStructureDefinition(
            new StructureDefinitionId("TestExampleResource"),
            new RawStructureDefinitionData(
                "TestExampleResource",
                "http://some.url/TestExampleResource",
                "draft",
                "Example Description",
                "resource",
                false,
                "TestExampleResource",
                null,
                new RawStructureDefinitionData.Differential(
                    new ArrayList<>(List.of(
                        new RawStructureDefinitionElementDefinition(
                            "TestExampleResource.exampleField",
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
            )
        )
    );

    this.thenLastDynamicEventOfNameApproved("AggregateDefinitionCreated");
  }
}