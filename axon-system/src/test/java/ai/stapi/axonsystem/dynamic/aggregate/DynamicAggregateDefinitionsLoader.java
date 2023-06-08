package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.schema.adHocLoaders.AbstractJavaModelDefinitionsLoader;
import ai.stapi.schema.scopeProvider.ScopeOptions;
import ai.stapi.schema.structuredefinition.ElementDefinition;
import ai.stapi.schema.structuredefinition.ElementDefinitionType;
import ai.stapi.schema.structuredefinition.StructureDefinitionData;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DynamicAggregateDefinitionsLoader extends
    AbstractJavaModelDefinitionsLoader<StructureDefinitionData> {

  public static final String SCOPE = "DynamicAggregateDefinitionsTest";
  public static final String TAG = ScopeOptions.TEST_TAG;

  protected DynamicAggregateDefinitionsLoader() {
    super(SCOPE, TAG, StructureDefinitionData.SERIALIZATION_TYPE);
  }

  @Override
  public List<StructureDefinitionData> load() {
    return List.of(
        new StructureDefinitionData(
            "ExampleDynamicAggregateType",
            "http://example.com/StructureDefinition/ExampleDynamicAggregateType",
            "active",
            "An example dynamic aggregate type",
            "resource",
            false,
            "ExampleDynamicAggregateType",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "ExampleDynamicAggregateType.exampleConstructorAttribute",
                        List.of(new ElementDefinitionType("string", null)),
                        1,
                        "1",
                        "An example constructor attribute",
                        "An example constructor attribute for the dynamic aggregate type",
                        null
                    ),
                    new ElementDefinition(
                        "ExampleDynamicAggregateType.exampleMethodAttribute",
                        List.of(new ElementDefinitionType("string", null)),
                        1,
                        "1",
                        "An example method attribute",
                        "An example method attribute for the dynamic aggregate type",
                        null
                    )
                ),
                "ExampleDynamicAggregateType"
            )
        ),
        new StructureDefinitionData(
            "ExampleDynamicAggregateTypeWithUnion",
            "http://example.com/StructureDefinition/ExampleDynamicAggregateTypeWithUnion",
            "active",
            "An example dynamic aggregate type",
            "resource",
            false,
            "ExampleDynamicAggregateTypeWithUnion",
            null,
            null,
            new StructureDefinitionData.Differential(
                List.of(
                    new ElementDefinition(
                        "ExampleDynamicAggregateTypeWithUnion.exampleUnionField",
                        List.of(
                            new ElementDefinitionType("string", null),
                            new ElementDefinitionType("Timing", null)
                        ),
                        1,
                        "1",
                        "An example union field",
                        "An example union field",
                        null
                    )
                ),
                "ExampleDynamicAggregateTypeWithUnion"
            )
        )
    );
  }
}
