package ai.stapi.graphql.graphqlJava;

import ai.stapi.graphql.GraphQlExecutor;
import ai.stapi.graphql.GraphQlOperation;
import ai.stapi.graphql.graphqlJava.testfixtures.TestGraphqlModelDefinitionsLoader;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(TestGraphqlModelDefinitionsLoader.SCOPE)
class GraphQlExecutorMutationTest extends DomainTestCase {

  @Autowired
  private GraphQlExecutor graphQlExecutor;

  @Test
  void itCanExecuteCreationalMutation() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                mutation {
                  createOrganization(
                    id: "pragueMedicalClinic",
                    payload: {
                      address: [
                        {
                          line: [
                            "Vaclavske Namesti 8",
                            "Vaclavske Namesti 11",
                            "Vaclavske Namesti 50"
                          ],
                          city: "Prague",
                          state: "Praha",
                          postalCode: "11000",
                          country: "Czech Republic",
                          position: {
                            latitude: 50.08804,
                            longitude: 14.42076
                          }
                        }
                      ],
                      telecom: [
                        {
                          system: "email",
                          value: "info@praguemedicalclinic.cz"
                        }
                      ]
                    }
                  ) {
                    successes
                  }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenLastEventGraphApproved();
  }
  
  @Test
  void itCanExecuteCreationalMutationForStructureDefinition() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                mutation {
                  createStructureDefinition(
                    id: "MyEntity",
                    payload: {
                      abstract: false,
                      baseDefinition: "http://hl7.org/fhir/StructureDefinition/DomainResource",
                      baseDefinitionRef: "DomainResource",
                      description: "An example entity",
                      differential: {
                        element: [
                          {
                            definition: "This is long description of an example entity (in most common language), or aggregate (in domain-driven design language), or resource (in HL7 FHIR standard). It will appear in graphQL documentation.",
                            id: "MyEntity",
                            isModifier: false,
                            max: "*",
                            min: 0,
                            mustSupport: false,
                            path: "MyEntity",
                            short: "his is long description of MyEntity"
                          },
                          {
                            definition: "A name associated with the MyEntity.",
                            id: "MyEntity.name",
                            isModifier: false,
                            isSummary: true,
                            max: "1",
                            min: 0,
                            mustSupport: false,
                            path: "MyEntity.name",
                            requirements: "Need to use the name as the label of the MyEntity.",
                            short: "Name used for the MyEntity resource",
                            type: [
                              {
                                code: "string",
                                codeRef: "string"
                              }
                            ]
                          }
                        ]
                      },
                      experimental: true,
                      kind: "resource",
                      name: "MyEntity",
                      status: "draft",
                      type: "MyEntity",
                      url: "http://myorganization.org/fhir/StructureDefinition/MyEntity",
                      version: "0.0.1",
                    }
                  ) {
                    successes
                  }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenLastEventGraphApproved();
  }

  @Test
  @Disabled
  void itCanExecuteCreationalMutationForStructureDefinitionWithDate() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                mutation {
                  createStructureDefinition(
                    id: "MyEntity",
                    payload: {
                      abstract: false,
                      baseDefinition: "http://hl7.org/fhir/StructureDefinition/DomainResource",
                      baseDefinitionRef: "DomainResource",
                      date: "2023-06-11T12:47:40",
                      description: "An example entity",
                      differential: {
                        element: [
                          {
                            definition: "This is long description of an example entity (in most common language), or aggregate (in domain-driven design language), or resource (in HL7 FHIR standard). It will appear in graphQL documentation.",
                            id: "MyEntity",
                            isModifier: false,
                            max: "*",
                            min: 0,
                            mustSupport: false,
                            path: "MyEntity",
                            short: "his is long description of MyEntity"
                          },
                          {
                            definition: "A name associated with the MyEntity.",
                            id: "MyEntity.name",
                            isModifier: false,
                            isSummary: true,
                            max: "1",
                            min: 0,
                            mustSupport: false,
                            path: "MyEntity.name",
                            requirements: "Need to use the name as the label of the MyEntity.",
                            short: "Name used for the MyEntity resource",
                            type: [
                              {
                                code: "string",
                                codeRef: "string"
                              }
                            ]
                          }
                        ]
                      },
                      experimental: true,
                      kind: "resource",
                      name: "MyEntity",
                      status: "draft",
                      type: "MyEntity",
                      url: "http://myorganization.org/fhir/StructureDefinition/MyEntity",
                      version: "0.0.1",
                    }
                  ) {
                    successes
                  }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenLastEventGraphApproved();
  }

  @Test
  void itCanExecuteCreationalMutationWithUnionParameter() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                mutation {
                  createOrganization(
                    id: "pragueMedicalClinic",
                    payload: {
                      level: { string: "VeryMuch" }
                    }
                  ) {
                    successes
                  }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenLastEventGraphApproved();
  }

  @Test
  void itCanExecuteCreationalMutationWithComplexParameterWithUnionField() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                mutation {
                  createOrganization(
                    id: "ExampleOrganization",
                    payload: {
                      address: [
                        {
                          extension: [
                              {
                                  url: "http://urlto.coding.extension",
                                  value: {
                                      Coding: {
                                          display: "DisplayName"
                                      }
                                  }
                              },
                              {
                                  url: "http://urlto.string.extension",
                                  value: {
                                      string: "Brnenice"
                                  }
                              }
                          ]
                          country: "Czech Republic",
                          city: "Brno"
                        }
                      ]
                    }
                  ) {
                    successes
                  }
                }
            """
    );
    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenLastEventGraphApproved();
  }
}