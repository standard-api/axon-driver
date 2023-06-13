package ai.stapi.graphql.generateGraphQlSchema;

import ai.stapi.schema.structureSchema.AbstractStructureType;
import ai.stapi.schema.structureSchema.BoxedPrimitiveStructureType;
import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.FieldDefinition;
import ai.stapi.schema.structureSchema.FieldType;
import ai.stapi.schema.structureSchema.PrimitiveStructureType;
import ai.stapi.schema.structureSchema.ResourceStructureType;
import ai.stapi.schema.structureSchema.StructureSchema;
import ai.stapi.schema.structureSchema.builder.StructureSchemaBuilder;
import ai.stapi.test.integration.IntegrationTestCase;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class GraphQlSchemaGeneratorTest extends IntegrationTestCase {

  @Autowired
  private GraphQlSchemaGenerator graphQlSchemaGenerator;

  @Test
  void itCanGenerateScalarType() {
    var graphDefinition = new StructureSchema(this.getBaseNeededDefinitions());

    var actual = this.graphQlSchemaGenerator.generate(graphDefinition, new ArrayList<>());
    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateResourceType() {
    var base = this.getBaseNeededDefinitions();

    var actual = this.graphQlSchemaGenerator.generate(new StructureSchema(base), new ArrayList<>());
    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateResourceTypeWithFirstLevelFields() {
    var graphDefinition = new StructureSchema(Map.of(
        "decimal", new PrimitiveStructureType(
            "decimal",
            "Base StructureDefinition for decimal Type: A rational number with implicit precision",
            false,
            null
        ),
        "string", new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type: A String description",
            false,
            null
        ),
        "ExampleResource", new ResourceStructureType(
            "ExampleResource",
            Map.of(
                "exampleListDecimalField", new FieldDefinition(
                    "exampleListDecimalField",
                    0,
                    "*",
                    "Example List Decimal Field on Example Resource",
                    List.of(FieldType.asPlainType("decimal")),
                    "ExampleResource"

                ),
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Resource",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleResource"
                )
            ),
            "Description of Example Resource",
            null,
            false
        ),
        "Element", new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    ));

    var actual = this.graphQlSchemaGenerator.generate(graphDefinition, new ArrayList<>());

    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateResourceTypeWithBaseDefinition() {
    var graphDefinition = new StructureSchemaBuilder(Map.of(
        "string", new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type: A String description",
            false,
            null
        ),
        "ExampleResource", new ResourceStructureType(
            "ExampleResource",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Resource",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleResource"
                )
            ),
            "Description of Example Resource",
            "ExampleBase",
            false
        ),
        "ExampleBase", new ResourceStructureType(
            "ExampleBase",
            Map.of(
                "exampleBaseStringField", new FieldDefinition(
                    "exampleBaseStringField",
                    1,
                    "1",
                    "Example String Field on Example Base",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleBase"
                )
            ),
            "Description of Example Base",
            null,
            false
        ),
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    )).build();

    var actual = this.graphQlSchemaGenerator.generate(graphDefinition, new ArrayList<>());

    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateComplexType() {
    var base = this.getBaseNeededDefinitions();
    base.put(
        "string",
        new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type: A String description",
            false,
            null
        )
    );
    base.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );

    base.put(
        "ExampleComplexType",
        new ComplexStructureType(
            "ExampleComplexType",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Complex Type",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleComplexType"
                )
            ),
            "Description of Example Complex Type",
            null,
            false
        )
    );

    var actual = this.graphQlSchemaGenerator.generate(new StructureSchema(base), new ArrayList<>());

    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateAbstractTypeAsInterface() {
    var base = this.getBaseNeededDefinitions();
    base.put(
        "string",
        new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type: A String description",
            false,
            null
        )
    );
    base.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );
    base.put(
        "ExampleComplexInterface",
        new ComplexStructureType(
            "ExampleComplexInterface",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Complex Interface",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleComplexInterface"
                )
            ),
            "Description of Example Complex Interface",
            null,
            true
        )
    );
    var actual = this.graphQlSchemaGenerator.generate(new StructureSchema(base), new ArrayList<>());

    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateComplexTypeWithInterfaceField() {
    var base = this.getBaseNeededDefinitions();
    base.put(
        "string",
        new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type: A String description",
            false,
            null
        )
    );
    base.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );
    base.put(
        "ExampleInterface",
        new ComplexStructureType(
            "ExampleInterface",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Interface",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleInterface"
                )
            ),
            "Description of Example Interface",
            null,
            true
        )
    );

    base.put(
        "ExampleComplexType",
        new ComplexStructureType(
            "ExampleComplexType",
            Map.of(
                "exampleInterfaceField", new FieldDefinition(
                    "exampleInterfaceField",
                    1,
                    "1",
                    "Example Interface Field on Example Complex Type",
                    List.of(FieldType.asPlainType("ExampleInterface")),
                    "ExampleComplexType"
                )
            ),
            "Description of Example Complex Type",
            null,
            false
        )
    );

    var actual = this.graphQlSchemaGenerator.generate(new StructureSchema(base), new ArrayList<>());

    this.thenObjectApproved(actual);
  }

  @Test
  void itShouldBeAbleToGenerateTypeWhichImplementsMultipleInterfaces() {
    var given = this.getBaseNeededDefinitions();
    given.put(
        "string",
        new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type: A String description",
            false,
            null
        )
    );
    given.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );
    given.put(
        "ExampleBaseInterface",
        new ComplexStructureType(
            "ExampleBaseInterface",
            Map.of(
                "exampleBaseInterfaceStringField", new FieldDefinition(
                    "exampleBaseInterfaceStringField",
                    1,
                    "1",
                    "Example Base Interface String Field on Example Base Interface",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleBaseInterface"
                )
            ),
            "Description of Example Base Interface",
            null,
            true
        )
    );

    given.put(
        "ExampleInterface",
        new ComplexStructureType(
            "ExampleInterface",
            Map.of(
                "exampleInterfaceStringField", new FieldDefinition(
                    "exampleInterfaceStringField",
                    1,
                    "1",
                    "Example Interface String Field on Example Interface",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleInterface"
                )
            ),
            "Description of Example Interface",
            "ExampleBaseInterface",
            true
        )
    );

    given.put(
        "ExampleComplexType",
        new ComplexStructureType(
            "ExampleComplexType",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Complex Type",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleComplexType"
                )
            ),
            "Description of Example Complex Type",
            "ExampleInterface",
            false
        )
    );

    var actual = this.graphQlSchemaGenerator.generate(
        new StructureSchemaBuilder(given).build(),
        new ArrayList<>()
    );
    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateTypeWithFieldOfAnyType() {
    var given = this.getBaseNeededDefinitions();

    var moreThan42TypeList = new ArrayList<FieldType>();
    for (int i = 0; i < 43; i++) {
      var typeName = "Type" + i;
      moreThan42TypeList.add(FieldType.asPlainType(typeName));
      given.put(
          typeName,
          new ComplexStructureType(
              typeName,
              Map.of(
                  "exampleFieldOn" + typeName, new FieldDefinition(
                      "exampleFieldOn" + typeName,
                      1,
                      "1",
                      "Example field " + i,
                      List.of(FieldType.asPlainType("decimal")),
                      typeName
                  )
              ),
              "Generated Description for type number: " + i,
              null,
              false
          )
      );
    }
    given.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );
    given.put(
        "ExampleComplexType",
        new ComplexStructureType(
            "ExampleComplexType",
            Map.of(
                "exampleAnyField", new FieldDefinition(
                    "exampleAnyField",
                    1,
                    "1",
                    "Example Any Field on Example Complex Type",
                    moreThan42TypeList,
                    "ExampleComplexType"
                )
            ),
            "Description of Example Complex Type",
            null,
            false
        )
    );

    var actual =
        this.graphQlSchemaGenerator.generate(new StructureSchema(given), new ArrayList<>());

    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateComplexTypeWithFieldOfUnionType() {
    var given = this.getBaseNeededDefinitions();
    given.put(
        "string",
        new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type: A String description",
            false,
            null
        )
    );

    given.put(
        "uri",
        new PrimitiveStructureType(
            "uri",
            "Base StructureDefinition for uri Type: A Uri description",
            false,
            null
        )
    );

    given.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "1",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                ),
                "extension", new FieldDefinition(
                    "extension",
                    0,
                    "*",
                    "Additional content defined by implementations",
                    List.of(FieldType.asPlainType("Extension")),
                    "Element"
                )
            ),
            "The base definition for all elements contained inside a resource. All elements, whether defined as a Data Type (including primitives) or as part of a resource structure, have this base content",
            null,
            true
        )
    );

    given.put(
        "Extension",
        new ComplexStructureType(
            "Extension",
            Map.of(
                "url", new FieldDefinition(
                    "url",
                    1,
                    "1",
                    "identifies the meaning of the extension",
                    List.of(FieldType.asPlainType("uri")),
                    "Extension"
                ),
                "value", new FieldDefinition(
                    "value",
                    0,
                    "1",
                    "Value of extension",
                    List.of(FieldType.asPlainType("string")),
                    "Extension"
                )
            ),
            "Every element in a resource or data type includes an optional \"extension\" child element that may be present any number of times. This is the content model of the extension as it appears in each resource",
            "Element",
            false
        )
    );

    given.put(
        "BoxedString",
        new BoxedPrimitiveStructureType(
            "BoxedString",
            "Base StructureDefinition for string Type: A String description",
            Map.of(
                "value", new FieldDefinition(
                    "value",
                    1,
                    "1",
                    "Primitive value for a string",
                    List.of(FieldType.asPlainType("string")),
                    "BoxedString"
                )
            ),
            false,
            "Element"
        )
    );

    given.put(
        "BoxedDecimal",
        new BoxedPrimitiveStructureType(
            "BoxedDecimal",
            "Base StructureDefinition for decimal Type: A rational number with implicit precision",
            Map.of(
                "value", new FieldDefinition(
                    "value",
                    1,
                    "1",
                    "Primitive value for a decimal",
                    List.of(FieldType.asPlainType("decimal")),
                    "BoxedDecimal"
                )
            ),
            false,
            "Element"
        )
    );

    given.put(
        "ExampleType",
        new ComplexStructureType(
            "ExampleType",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Type",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleType"
                )
            ),
            "Description of ExampleType",
            null,
            false
        )
    );

    given.put(
        "ExampleComplexType",
        new ComplexStructureType(
            "ExampleComplexType",
            Map.of(
                "exampleUnionTypeField", new FieldDefinition(
                    "exampleUnionTypeField",
                    1,
                    "1",
                    "Example Union Type Field on Example Complex Type",
                    List.of(
                        FieldType.asBoxedType("string"),
                        FieldType.asBoxedType("decimal"),
                        FieldType.asPlainType("ExampleType")
                    ),
                    "ExampleComplexType"
                )
            ),
            "Description of ExampleComplexType",
            null,
            false
        )
    );

    var graphDefinition = new StructureSchemaBuilder(given).build();
    var actual = this.graphQlSchemaGenerator.generate(graphDefinition, new ArrayList<>());

    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateComplexTypeWithFieldOfUnionTypeWithInterfaceAsOneOfItsMembers() {
    var given = this.getBaseNeededDefinitions();
    given.put(
        "string",
        new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type: A String description",
            false,
            null
        )
    );

    given.put(
        "ExampleType",
        new ComplexStructureType(
            "ExampleType",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Type",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleType"
                )
            ),
            "Description of ExampleType",
            null,
            false
        )
    );

    given.put(
        "ExampleInheritingType1",
        new ComplexStructureType(
            "ExampleInheritingType1",
            Map.of(),
            "Description of ExampleInheritingType1",
            "ExampleInterfaceType",
            false
        )
    );

    given.put(
        "ExampleInheritingType2",
        new ComplexStructureType(
            "ExampleInheritingType2",
            Map.of(),
            "Description of ExampleInheritingType2",
            "ExampleInterfaceType",
            false
        )
    );

    given.put(
        "ExampleInterfaceType",
        new ComplexStructureType(
            "ExampleInterfaceType",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Interface Type",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleInterfaceType"
                )
            ),
            "Description of Example Interface Type",
            null,
            true
        )
    );
    given.put(
        "ExampleComplexType",
        new ComplexStructureType(
            "ExampleComplexType",
            Map.of(
                "exampleUnionTypeField", new FieldDefinition(
                    "exampleUnionTypeField",
                    1,
                    "1",
                    "Example Union Type Field on Example Complex Type",
                    List.of(
                        FieldType.asPlainType("ExampleInterfaceType"),
                        FieldType.asPlainType("ExampleType")
                    ),
                    "ExampleComplexType"
                )),
            "Description of ExampleComplexType",
            null,
            false
        )
    );
    given.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );

    var graphDefinition = new StructureSchemaBuilder(given).build();
    var actual = this.graphQlSchemaGenerator.generate(graphDefinition, new ArrayList<>());

    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateMutationWithPrimitiveFields() {
    var base = this.getBaseNeededDefinitions();
    var actual = this.graphQlSchemaGenerator.generate(
        new StructureSchema(base),
        List.of(
            new ComplexStructureType(
                "CreateExampleResource",
                Map.of(
                    "exampleListDecimalField", new FieldDefinition(
                        "exampleListDecimalField",
                        0,
                        "*",
                        "",
                        List.of(
                            FieldType.asPlainType("decimal")
                        ),
                        "CreateExampleResource"
                    )
                ),
                "Example Mutation For Creating Example Resource",
                null,
                false
            )
        )
    );
    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateMutationWithComplexField() {
    var given = new HashMap<>(Map.of(
        "decimal", new PrimitiveStructureType(
            "decimal",
            "Base StructureDefinition for decimal Type: A rational number with implicit precision",
            false,
            null
        ),
        "string", new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type",
            false,
            null
        ),
        "ExampleResource", new ResourceStructureType(
            "ExampleResource",
            Map.of(
                "exampleListDecimalField", new FieldDefinition(
                    "exampleListDecimalField",
                    0,
                    "*",
                    "Example List Decimal Field on Example Resource",
                    List.of(FieldType.asPlainType("decimal")),
                    "ExampleResource"
                ),
                "exampleComplexField", new FieldDefinition(
                    "exampleComplexField",
                    1,
                    "1",
                    "Example Complex Field on Example Resource",
                    List.of(FieldType.asPlainType("ExampleComplexType")),
                    "ExampleResource"
                )
            ),
            "Description of Example Resource",
            null,
            false
        )
    ));

    given.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );

    given.put(
        "ExampleComplexType",
        new ComplexStructureType(
            "ExampleComplexType",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Complex Type",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleComplexType"
                )
            ),
            "Description of Example Complex Type",
            null,
            false
        )
    );

    var actual = this.graphQlSchemaGenerator.generate(
        new StructureSchema(given),
        List.of(
            new ComplexStructureType(
                "CreateExampleResource",
                Map.of(
                    "exampleListDecimalField", new FieldDefinition(
                        "exampleListDecimalField",
                        0,
                        "*",
                        "",
                        List.of(
                            FieldType.asPlainType("decimal")
                        ),
                        "CreateExampleResource"
                    ),
                    "exampleComplexField", new FieldDefinition(
                        "exampleComplexField",
                        1,
                        "1",
                        "",
                        List.of(
                            FieldType.asPlainType("ExampleComplexType")
                        ),
                        "CreateExampleResource"
                    )
                ),
                "Example Mutation For Creating Example Resource",
                null,
                false
            )
        )
    );
    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateMutationWithComplexFieldOfTypeWithUnionField() {
    var given = new HashMap<>(Map.of(
        "decimal", new PrimitiveStructureType(
            "decimal",
            "Base StructureDefinition for decimal Type: A rational number with implicit precision",
            false,
            null
        ),
        "string", new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type",
            false,
            null
        ),
        "ExampleResource", new ResourceStructureType(
            "ExampleResource",
            Map.of(
                "exampleListDecimalField", new FieldDefinition(
                    "exampleListDecimalField",
                    0,
                    "*",
                    "Example List Decimal Field on Example Resource",
                    List.of(FieldType.asPlainType("decimal")),
                    "ExampleResource"
                ),
                "exampleUnionField", new FieldDefinition(
                    "exampleUnionField",
                    1,
                    "1",
                    "Example Union Field on Example Resource",
                    List.of(FieldType.asPlainType("ExampleComplexType")),
                    "ExampleResource"
                )
            ),
            "Description of Example Resource",
            null,
            false
        )
    ));
    given.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );
    given.put(
        "ExampleComplexType",
        new ComplexStructureType(
            "ExampleComplexType",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    1,
                    "1",
                    "Example String Field on Example Complex Type",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleComplexType"
                ),
                "exampleUnionField", new FieldDefinition(
                    "exampleUnionField",
                    1,
                    "5",
                    "Example Union Field on Example Complex Type",
                    List.of(new FieldType("BoxedString", "string"),
                        new FieldType("BoxedDecimal", "decimal")),
                    "ExampleComplexType"
                )
            ),
            "Description of Example Complex Type",
            null,
            false
        )
    );

    var actual = this.graphQlSchemaGenerator.generate(
        new StructureSchemaBuilder(given).build(),
        List.of(
            new ComplexStructureType(
                "CreateExampleResource",
                Map.of(
                    "exampleListDecimalField", new FieldDefinition(
                        "exampleListDecimalField",
                        0,
                        "*",
                        "",
                        List.of(
                            FieldType.asPlainType("decimal")
                        ),
                        "CreateExampleResource"
                    ),
                    "exampleComplexField", new FieldDefinition(
                        "exampleComplexField",
                        1,
                        "1",
                        "",
                        List.of(
                            FieldType.asPlainType("ExampleComplexType")
                        ),
                        "CreateExampleResource"
                    )
                ),
                "Example Mutation For Creating Example Resource",
                null,
                false
            )
        )
    );
    this.thenObjectApproved(actual);
  }

  @Test
  void itCanGenerateMutationWithReferenceToAnotherResource() {
    var given = new HashMap<>(Map.of(
        "decimal", new PrimitiveStructureType(
            "decimal",
            "Base StructureDefinition for decimal Type: A rational number with implicit precision",
            false,
            null
        ),
        "string", new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type",
            false,
            null
        ),
        "ExampleResource", new ResourceStructureType(
            "ExampleResource",
            Map.of(
                "exampleListDecimalField", new FieldDefinition(
                    "exampleListDecimalField",
                    0,
                    "*",
                    "Example List Decimal Field on Example Resource",
                    List.of(FieldType.asPlainType("decimal")),
                    "ExampleResource"
                ),
                "exampleResourceField", new FieldDefinition(
                    "exampleResourceField",
                    1,
                    "1",
                    "Example Resource Field on Example Resource",
                    List.of(FieldType.asPlainType("ExampleReferencedResource")),
                    "ExampleResource"
                )
            ),
            "Description of Example Resource",
            null,
            false
        ),
        "ExampleReferencedResource", new ResourceStructureType(
            "ExampleReferencedResource",
            Map.of(
                "exampleStringField", new FieldDefinition(
                    "exampleStringField",
                    0,
                    "1",
                    "Example String Field on Referenced Resource",
                    List.of(FieldType.asPlainType("string")),
                    "ExampleReferencedResource"
                )
            ),
            "Description of Example Referenced Resource",
            null,
            false
        )
    ));
    given.put(
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        )
    );
    var actual = this.graphQlSchemaGenerator.generate(
        new StructureSchema(given),
        List.of(
            new ComplexStructureType(
                "CreateExampleResource",
                Map.of(
                    "exampleListDecimalField", new FieldDefinition(
                        "exampleListDecimalField",
                        0,
                        "*",
                        "",
                        List.of(
                            FieldType.asPlainType("decimal")
                        ),
                        "CreateExampleResource"
                    ),
                    "exampleResourceField", new FieldDefinition(
                        "exampleResourceField",
                        1,
                        "1",
                        "",
                        List.of(
                            new FieldType("ExampleReferencedResource", "Reference")
                        ),
                        "CreateExampleResource"
                    )
                ),
                "Example Mutation For Creating Example Resource",
                null,
                false
            )
        )
    );
    this.thenObjectApproved(actual);
  }

  @NotNull
  private Map<String, AbstractStructureType> getBaseNeededDefinitions() {
    return new HashMap<>(Map.of(
        "decimal", new PrimitiveStructureType(
            "decimal",
            "Base StructureDefinition for decimal Type: A rational number with implicit precision",
            false,
            null
        ),
        "string", new PrimitiveStructureType(
            "string",
            "Base StructureDefinition for string Type",
            false,
            null
        ),
        "Element",
        new ComplexStructureType(
            "Element",
            Map.of(
                "id", new FieldDefinition(
                    "id",
                    0,
                    "*",
                    "Unique id for inter-element referencing",
                    List.of(FieldType.asPlainType("string")),
                    "Element"
                )
            ),
            "",
            "",
            false
        ),
        "ExampleResource", new ResourceStructureType(
            "ExampleResource",
            Map.of(
                "exampleListDecimalField", new FieldDefinition(
                    "exampleListDecimalField",
                    0,
                    "*",
                    "Example List Decimal Field on Example Resource",
                    List.of(FieldType.asPlainType("decimal")
                    ),
                    "ExampleResource"
                )),
            "Description of Example Resource",
            null,
            false
        )
    ));
  }
}