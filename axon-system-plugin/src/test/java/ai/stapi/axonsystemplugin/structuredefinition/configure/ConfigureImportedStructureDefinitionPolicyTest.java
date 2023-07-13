package ai.stapi.axonsystemplugin.structuredefinition.configure;

import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.ImportStructureDefinition;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.StructureDefinitionImported;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.ElementDefinitionType;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionData;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionElementDefinition;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import ai.stapi.test.domain.DomainTestCase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("dev")
class ConfigureImportedStructureDefinitionPolicyTest extends DomainTestCase {

  @Autowired
  private StructureSchemaFinder structureSchemaFinder;
  
  @Test
  void itWillConfigureNewStructureDefinition() {
    var testComplex = new RawStructureDefinitionData(
        "TestComplex",
        "http://test.url/for/complex",
        "draft",
        "Structure Definition for test complex ",
        "complex-type",
        false,
        "TestComplex",
        null,
        new RawStructureDefinitionData.Differential(
            new ArrayList<>(
                List.of(
                    new RawStructureDefinitionElementDefinition(
                        "TestComplex.somePrimitiveField",
                        1,
                        "1",
                        "Primitive Example Field on Test Complex",
                        "",
                        "",
                        new ArrayList<>(
                            List.of(
                                new RawStructureDefinitionElementDefinition.ElementDefinitionType(
                                    "string",
                                    new ArrayList<>()
                                )
                            )
                        )
                    )
                )
            )
        )
    );
    this.whenCommandIsDispatched(
        new ImportStructureDefinition(
            new StructureDefinitionId("TestComplex"),
            testComplex
        )
    );
    this.thenExpectedEventTypesSaved(
        StructureDefinitionImported.class,
        StructureDefinitionConfigured.class
    );
    var field = this.structureSchemaFinder.getFieldDefinitionFor(
        "TestComplex",
        "somePrimitiveField"
    );
    this.thenObjectApproved(field);
  }

  @Test
  void itWillConfigureAddedElement() {
    var testComplex = new RawStructureDefinitionData(
        "TestComplex",
        "http://test.url/for/complex",
        "draft",
        "Structure Definition for test complex ",
        "complex-type",
        false,
        "TestComplex",
        null,
        new RawStructureDefinitionData.Differential(
            new ArrayList<>(
                List.of(
                    new RawStructureDefinitionElementDefinition(
                        "TestComplex.somePrimitiveField",
                        1,
                        "1",
                        "Primitive Example Field on Test Complex",
                        "",
                        "",
                        new ArrayList<>(
                            List.of(
                                new RawStructureDefinitionElementDefinition.ElementDefinitionType(
                                    "string",
                                    new ArrayList<>()
                                )
                            )
                        )
                    )
                )
            )
        )
    );
    this.givenCommandIsDispatched(
        new ImportStructureDefinition(
            new StructureDefinitionId("TestComplex"),
            testComplex
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            new UniqueIdentifier("TestComplex"),
            "AddElementOnStructureDefinitionDifferential",
            Map.of(
                "element", new ArrayList<>(List.of(
                    new ElementDefinition(
                        "TestComplex.newField",
                        new ArrayList<>(List.of(new ElementDefinitionType("string"))),
                        0,
                        "1",
                        "New field",
                        "New field",
                        "New field"
                    )
                ))
            )
        )
    );
    this.thenExpectedEventTypesSaved(
        StructureDefinitionImported.class,
        StructureDefinitionConfigured.class,
        DynamicGraphUpdatedEvent.class,
        ElementsToStructureDefinitionConfigured.class
    );
    var field = this.structureSchemaFinder.getFieldDefinitionFor(
        "TestComplex",
        "newField"
    );
    this.thenObjectApproved(field);
  }

  @Test
  @Disabled
  void itCanImportStructureDefinitionAndExecuteAppropriateDynamicCommand() {
    var testPrimitive = new RawStructureDefinitionData(
        "string",
        "http://test.url/for/primitive",
        "draft",
        "Structure Definition for string",
        "primitive-type",
        false,
        "string",
        null,
        new RawStructureDefinitionData.Differential(new ArrayList<>())
    );
    var testComplex = new RawStructureDefinitionData(
        "TestComplex",
        "http://test.url/for/complex",
        "draft",
        "Structure Definition for test complex ",
        "complex-type",
        false,
        "TestComplex",
        null,
        new RawStructureDefinitionData.Differential(
            new ArrayList<>(
                List.of(
                    new RawStructureDefinitionElementDefinition(
                        "TestComplex.somePrimitiveField",
                        1,
                        "1",
                        "Primitive Example Field on Test Complex",
                        "",
                        "",
                        new ArrayList<>(
                            List.of(
                                new RawStructureDefinitionElementDefinition.ElementDefinitionType(
                                    "string",
                                    new ArrayList<>()
                                )
                            )
                        )
                    )
                )
            )
        )
    );
    var testResource = new RawStructureDefinitionData(
        "TestResource",
        "http://test.url/for/Resource",
        "draft",
        "Structure Definition for test Resource ",
        "resource",
        false,
        "TestResource",
        null,
        new RawStructureDefinitionData.Differential(
            new ArrayList<>(
                List.of(
                    new RawStructureDefinitionElementDefinition(
                        "TestResource.somePrimitiveField",
                        1,
                        "1",
                        "Primitive Example Field on Test Resource",
                        "",
                        "",
                        new ArrayList<>(
                            List.of(
                                new RawStructureDefinitionElementDefinition.ElementDefinitionType(
                                    "string",
                                    new ArrayList<>()
                                )
                            )
                        )
                    ),
                    new RawStructureDefinitionElementDefinition(
                        "TestResource.someComplexField",
                        1,
                        "1",
                        "Complex Example Field on Test Resource",
                        "",
                        "",
                        new ArrayList<>(
                            List.of(
                                new RawStructureDefinitionElementDefinition.ElementDefinitionType(
                                    "TestComplex",
                                    new ArrayList<>()
                                )
                            )
                        )
                    )
                )
            )
        )
    );
    var element = new RawStructureDefinitionData(
        "Element",
        "http://test.url/for/Element",
        "draft",
        "Structure Definition for string",
        "complex-type",
        true,
        "Element",
        null,
        new RawStructureDefinitionData.Differential(new ArrayList<>())
    );
    this.givenCommandIsDispatched(
        new ImportStructureDefinition(
            new StructureDefinitionId("Element"),
            element
        )
    );
    this.givenCommandIsDispatched(
        new ImportStructureDefinition(
            new StructureDefinitionId("string"),
            testPrimitive
        )
    );
    this.givenCommandIsDispatched(
        new ImportStructureDefinition(
            new StructureDefinitionId("TestComplex"),
            testComplex
        )
    );
    this.givenCommandIsDispatched(
        new ImportStructureDefinition(
            new StructureDefinitionId("TestResource"),
            testResource
        )
    );
    this.whenCommandIsDispatched(
        new DynamicCommand(
            UniversallyUniqueIdentifier.randomUUID(),
            "CreateTestResource",
            new HashMap<>(
                Map.of(
                    "somePrimitiveField", "Primitive Value",
                    "someComplexField", new HashMap<>(Map.of(
                        "somePrimitiveField", "Example Primitive Value in Complex Value"
                    )
                    )
                )
            )
        ));
    this.thenLastEventOfTypeGraphApproved(DynamicGraphUpdatedEvent.class);
  }
}