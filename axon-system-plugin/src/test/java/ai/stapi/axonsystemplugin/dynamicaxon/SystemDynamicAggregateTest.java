package ai.stapi.axonsystemplugin.dynamicaxon;

import ai.stapi.graphsystem.aggregategraphstatemodifier.exceptions.CannotAddToAggregateState;
import ai.stapi.graphsystem.messaging.command.DynamicCommand;
import ai.stapi.graphsystem.messaging.event.DynamicGraphUpdatedEvent;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.ImportStructureDefinition;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.StructureDefinitionImported;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionData;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionElementDefinition;
import ai.stapi.schema.structuredefinition.RawStructureDefinitionElementDefinition.ElementDefinitionType;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import ai.stapi.test.domain.DomainTestCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;

class SystemDynamicAggregateTest extends DomainTestCase {
  
  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void itCannotAddItemToListFieldIfTheSubnodeTobeModifiedDoesntExist() throws IOException {
    this.givenStructureForElementWithoutDifferentialCreated();
    var command = new DynamicCommand(
        new StructureDefinitionId("Element"),
        "AddElementOnStructureDefinitionDifferential",
        new HashMap<>(Map.of(
            "element", new ArrayList<>(List.of(
                new RawStructureDefinitionElementDefinition(
                    "Element.newField",
                    0,
                    "1",
                    "A newly added element",
                    "A newly added element",
                    "A newly added element",
                    new ArrayList<>(List.of(
                        new ElementDefinitionType(
                            "string",
                            new ArrayList<>()
                        )
                    ))
                )
            ))
        ))
    );
    Executable executable = () -> this.whenCommandIsDispatched(command);
    this.thenExceptionMessageApproved(CannotAddToAggregateState.class, executable);
  }

  @Test
  void itCanAddItemToListFieldOnSubnode() throws IOException {
    this.givenStructureForElementCreated();
    var command = new DynamicCommand(
        new StructureDefinitionId("Element"),
        "AddElementOnStructureDefinitionDifferential",
        new HashMap<>(Map.of(
            "element", new ArrayList<>(List.of(
                new RawStructureDefinitionElementDefinition(
                    "Element.newField",
                    0,
                    "1",
                    "A newly added element",
                    "A newly added element",
                    "A newly added element",
                    new ArrayList<>(List.of(
                        new ElementDefinitionType(
                            "string",
                            new ArrayList<>()
                        )
                    ))
                )
            ))
        ))
    );
    this.whenCommandIsDispatched(command);
    this.thenExpectedEventTypesSaved(
        StructureDefinitionImported.class,
        DynamicGraphUpdatedEvent.class
    );
    this.thenLastDynamicEventOfNameApproved("ElementOnStructureDefinitionDifferentialAdded");
  }

  @Test
  void itCanAddMultipleItemsToListFieldOnSubnode() throws IOException {
    this.givenStructureForElementCreated();
    var command = new DynamicCommand(
        new StructureDefinitionId("Element"),
        "AddElementOnStructureDefinitionDifferential",
        new HashMap<>(Map.of(
            "element", new ArrayList<>(List.of(
                new RawStructureDefinitionElementDefinition(
                    "Element.newField",
                    0,
                    "1",
                    "A newly added element",
                    "A newly added element",
                    "A newly added element",
                    new ArrayList<>(List.of(
                        new ElementDefinitionType(
                            "string",
                            new ArrayList<>()
                        )
                    ))
                ),
                new RawStructureDefinitionElementDefinition(
                    "Element.anotherField",
                    0,
                    "1",
                    "A anotherField added element",
                    "A anotherField added element",
                    "A anotherField added element",
                    new ArrayList<>(List.of(
                        new ElementDefinitionType(
                            "string",
                            new ArrayList<>()
                        )
                    ))
                )
            ))
        ))
    );
    this.whenCommandIsDispatched(command);
    this.thenExpectedEventTypesSaved(
        StructureDefinitionImported.class,
        DynamicGraphUpdatedEvent.class
    );
    this.thenLastDynamicEventOfNameApproved("ElementOnStructureDefinitionDifferentialAdded");
  }

  @Test
  void itCannotAddItemToListFieldLocatedInListOfSubnodesWhenIdOfSubnodeNotFound()
      throws IOException {
    this.givenStructureForElementCreated();
    var command = new DynamicCommand(
        new StructureDefinitionId("Element"),
        "AddCodingOnStructureDefinitionJurisdiction",
        new HashMap<>(Map.of(
            "coding", new ArrayList<>(List.of(
                new HashMap<>(Map.of(
                    "code", "Some Jurisdiction Code"
                ))
            )),
            "jurisdictionId", "Jurisdiction/NotExistingJurisdiction"
        ))
    );
    Executable executable = () -> this.whenCommandIsDispatched(command);
    this.thenExceptionMessageApproved(CannotAddToAggregateState.class, executable);
  }

  @Test
  void itCanAddItemToListFieldLocatedInListOfSubnodes() throws IOException {
    this.givenStructureForElementCreated();
    var jurisdictionId = "JurisdictionId";
    var structureId = new StructureDefinitionId("Element");
    var givenCommand = new DynamicCommand(
        structureId,
        "AddJurisdictionOnStructureDefinition",
        new HashMap<>(Map.of(
            "jurisdiction", new ArrayList<>(List.of(
                new HashMap<>(Map.of(
                    "id", jurisdictionId,
                    "text", "Some Jurisdiction Text"
                ))
            ))
        ))
    );
    this.givenCommandIsDispatched(givenCommand);
    var command = new DynamicCommand(
        structureId,
        "AddCodingOnStructureDefinitionJurisdiction",
        new HashMap<>(Map.of(
            "coding", new ArrayList<>(List.of(
                new HashMap<>(Map.of(
                    "code", "Some Jurisdiction Code"
                ))
            )),
            "jurisdictionId", String.format("CodeableConcept/%s", jurisdictionId)
        ))
    );
    this.whenCommandIsDispatched(command);
    this.thenExpectedEventTypesSaved(
        StructureDefinitionImported.class,
        DynamicGraphUpdatedEvent.class,
        DynamicGraphUpdatedEvent.class
    );
    this.thenExpectedDynamicEventsSaved(
        "JurisdictionOnStructureDefinitionAdded",
        "CodingOnStructureDefinitionJurisdictionAdded"
    );
    this.thenMergedGraphOfAggregateApproved(structureId);
  }

  @Test
  void itCanAddItemToListFieldLocatedInListFieldOfListOfSubnodes() throws IOException {
    //Given
    this.givenStructureForElementCreated();
    var structureId = new StructureDefinitionId("Element");
    var exampleElementId = "ExampleElementId";
    var addElementCommand = new DynamicCommand(
        structureId,
        "AddElementOnStructureDefinitionDifferential",
        new HashMap<>(Map.of(
            "element", new ArrayList<>(List.of(
                Map.of(
                    "id", exampleElementId,
                    "path", "Element.newField",
                    "min", 0,
                    "max", "1",
                    "shortDescription", "A newly added element",
                    "definition", "A newly added element",
                    "comment", "A newly added element",
                    "type", new ArrayList<>(List.of(
                        new ElementDefinitionType(
                            "string",
                            new ArrayList<>()
                        )
                    ))
                )
            ))
        ))
    );
    this.givenCommandIsDispatched(addElementCommand);
    var exampleTypeId = "ExampleTypeId";
    var addElementTypeCommand = new DynamicCommand(
        structureId,
        "AddTypeOnStructureDefinitionDifferentialElement",
        new HashMap<>(Map.of(
            "elementId", String.format("ElementDefinition/%s", exampleElementId),
            "type", new ArrayList<>(List.of(
                Map.of(
                    "id", exampleTypeId,
                    "code", "Reference"
                )
            ))
        ))
    );
    this.givenCommandIsDispatched(addElementTypeCommand);
    var command = new DynamicCommand(
        structureId,
        "AddTargetProfileOnStructureDefinitionDifferentialElementType",
        new HashMap<>(Map.of(
            "typeId", String.format("ElementDefinitionType/%s", exampleTypeId),
            "targetProfile", new ArrayList<>(List.of(
                "SomeTargetProfile"
            ))
        ))
    );
    this.whenCommandIsDispatched(command);
    this.thenExpectedEventTypesSaved(
        StructureDefinitionImported.class,
        DynamicGraphUpdatedEvent.class,
        DynamicGraphUpdatedEvent.class,
        DynamicGraphUpdatedEvent.class
    );
    this.thenExpectedDynamicEventsSaved(
        "ElementOnStructureDefinitionDifferentialAdded",
        "TypeOnStructureDefinitionDifferentialElementAdded",
        "TargetProfileOnStructureDefinitionDifferentialElementTypeAdded"
    );
    this.thenMergedGraphOfAggregateApproved(structureId);
  }

  //TODO: uncomment after aggregate definition loaders
//  @Test
//  void itCanAddItemToListFieldOfUnionMember() {
//    var structureId = new UniqueIdentifier("ExampleId");
//    this.givenCommandIsDispatched(
//        new DynamicCommand(
//            structureId,
//            "CreateExampleDynamicAggregateTypeWithUnion",
//            Map.of(
//                "exampleUnionField", new HashMap<>(Map.of(
//                    "serializationType", "Timing",
//                    "code", new HashMap<>(Map.of(
//                        "text", "Some Irrelevant Timing code text"
//                    ))
//                ))
//            )
//        )
//    );
//    this.whenCommandIsDispatched(
//        new DynamicCommand(
//            structureId,
//            "AddEventOnExampleDynamicAggregateTypeWithUnionExampleUnionField",
//            Map.of(
//                "exampleUnionField", new HashMap<>(Map.of(
//                    "event", "Pica"
//                ))
//            )
//        )
//    );
//    this.thenExpectedDynamicEventsSaved(
//        "ElementOnStructureDefinitionDifferentialAdded",
//        "TypeOnStructureDefinitionDifferentialElementAdded",
//        "TargetProfileOnStructureDefinitionDifferentialElementTypeAdded"
//    );
//    this.thenMergedGraphOfAggregateApproved(structureId);
//  }

  private void givenStructureForElementCreated() throws IOException {
    var fixtureFilePath = this.getFixtureFilePath("element.profile.canonical.json");
    var file = new File(fixtureFilePath);
    var fileContent = FileUtils.readFileToString(file, "UTF-8");
    RawStructureDefinitionData structureDefinitionSource;
    try {
      structureDefinitionSource = this.objectMapper.readValue(fileContent, RawStructureDefinitionData.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    var givenCommand = new ImportStructureDefinition(
        new StructureDefinitionId("Element"),
        structureDefinitionSource
    );
    this.givenCommandIsDispatched(givenCommand);
  }

  private void givenStructureForElementWithoutDifferentialCreated() throws IOException {
    var fixtureFilePath = this.getFixtureFilePath("element.profile.canonical.json");
    var file = new File(fixtureFilePath);
    var fileContent = FileUtils.readFileToString(file, "UTF-8");
    RawStructureDefinitionData structureDefinitionSource;
    try {
      structureDefinitionSource =
          this.objectMapper.readValue(fileContent, RawStructureDefinitionData.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    structureDefinitionSource = new RawStructureDefinitionData(
        structureDefinitionSource.getId(),
        structureDefinitionSource.getUrl(),
        structureDefinitionSource.getStatus(),
        structureDefinitionSource.getDescription(),
        structureDefinitionSource.getKind(),
        structureDefinitionSource.getIsAbstract(),
        structureDefinitionSource.getType(),
        structureDefinitionSource.getBaseDefinition(),
        null
    );
    var givenCommand = new ImportStructureDefinition(
        new StructureDefinitionId("Element"),
        structureDefinitionSource
    );
    this.givenCommandIsDispatched(givenCommand);
  }
}
