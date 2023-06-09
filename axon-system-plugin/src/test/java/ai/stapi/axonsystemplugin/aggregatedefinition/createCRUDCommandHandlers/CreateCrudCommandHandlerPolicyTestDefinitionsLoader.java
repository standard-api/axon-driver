package ai.stapi.axonsystemplugin.aggregatedefinition.createCRUDCommandHandlers;

import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.schema.adHocLoaders.AbstractJavaModelDefinitionsLoader;
import ai.stapi.schema.scopeProvider.ScopeOptions;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.ElementDefinitionType;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CreateCrudCommandHandlerPolicyTestDefinitionsLoader
    extends AbstractJavaModelDefinitionsLoader<StructureDefinitionData> {

  public static final String TAG = ScopeOptions.TEST_TAG;
  public static final String SCOPE = "CreateCrudCommandHandlerPolicyTest";

  protected CreateCrudCommandHandlerPolicyTestDefinitionsLoader() {
    super(SCOPE, TAG, StructureDefinitionData.SERIALIZATION_TYPE);
  }

  @Override
  public List<StructureDefinitionData> load() {
    return List.of(
        new StructureDefinitionData(
            "ExampleTestResource",
            "http://example.com/StructureDefinition/ExampleTestResource",
            "active",
            "An example FHIR resource for testing purposes",
            "resource",
            false,
            "ExampleTestResource",
            "http://hl7.org/fhir/StructureDefinition/DomainResource",
            new UniqueIdentifier("DomainResource"),
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "ExampleTestResource.name",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "The name of the test resource",
                        "The name of the test resource",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleTestResource.description",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "1",
                        "The description of the test resource",
                        "The description of the test resource",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleTestResource.tag",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "Tags of the test resource",
                        "Tags of the test resource",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleTestResource.structure",
                        List.of(
                            new ElementDefinitionType(
                                "Reference",
                                new UniqueIdentifier("Reference"),
                                List.of("http://hl7.org/fhir/StructureDefinition/StructureDefinition"),
                                List.of(new UniqueIdentifier("StructureDefinition"))
                            )
                        ),
                        0,
                        "1",
                        "The structure of the test resource",
                        "The structure of the test resource",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "SameFieldComplex1",
            "http://example.com/StructureDefinition/SameFieldComplex1",
            "active",
            "An example FHIR resource for testing purposes",
            "complex-type",
            false,
            "SameFieldComplex1",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "SameFieldComplex1.sameField",
                        List.of(new ElementDefinitionType("string")),
                        1,
                        "*",
                        "The same field but with type string",
                        "The same field but with type string",
                        null
                    )
                )
            )
        ),
        new StructureDefinitionData(
            "SameFieldComplex2",
            "http://example.com/StructureDefinition/SameFieldComplex2",
            "active",
            "An example FHIR resource for testing purposes",
            "complex-type",
            false,
            "SameFieldComplex2",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "SameFieldComplex2.sameField",
                        List.of(new ElementDefinitionType("decimal")),
                        1,
                        "*",
                        "The same field but with type decimal",
                        "The same field but with type decimal",
                        null
                    )
                )
            )
        )
    );
  }
}
