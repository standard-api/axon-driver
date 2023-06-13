package ai.stapi.graphql.graphqlJava;

import ai.stapi.graph.repositorypruner.RepositoryPruner;
import ai.stapi.graphoperations.synchronization.GraphSynchronizer;
import ai.stapi.graphql.GraphQlExecutor;
import ai.stapi.graphql.GraphQlOperation;
import ai.stapi.graphql.graphqlJava.testfixtures.ShowcaseGraphFixturesProvider;
import ai.stapi.graphql.graphqlJava.testfixtures.TestGraphqlModelDefinitionsLoader;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import ai.stapi.test.systemschema.SystemSchemaIntegrationTestCase;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(TestGraphqlModelDefinitionsLoader.SCOPE)
class GraphQlExecutorTest extends SystemSchemaIntegrationTestCase {

  @Autowired
  private GraphQlExecutor graphQlExecutor;

  @BeforeAll
  @Order(2)
  public static void beforeAll(
      @Autowired GraphSynchronizer graphSynchronizer,
      @Autowired RepositoryPruner repositoryPruner,
      @Autowired ShowcaseGraphFixturesProvider showcaseGraphFixturesProvider
  ) {
    repositoryPruner.prune();
    graphSynchronizer.synchronize(showcaseGraphFixturesProvider.getFixtureGraph());
  }

  @Test
  void itCanGetStructureDefinition() {
    var graphQlRequest = new GraphQlOperation(
        "",
        "query { getStructureDefinition(id: \"CodeableConcept\") { id kind abstract } }"
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFindStructureDefinitions() {
    var graphQlRequest = new GraphQlOperation(
        "",
        "query { findStructureDefinitionList(sort: [ { type: ASC } ]) { id kind abstract } }"
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanGetStructureDefinitionAndItsElements() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    getStructureDefinition(id: "CodeableConcept") {
                        id
                        kind
                        abstract
                        differential {
                            element(
                                pagination: { limit: 100, offset: 0 },
                                sort: [
                                    { path: ASC }
                                ]
                            ) {
                                id
                                path
                                min
                                max
                            }
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFindStructureDefinitionsAndGetOneAtTheSameTime() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findStructureDefinitionList(sort: [ { type: ASC } ]) {
                        id
                        kind
                        abstract
                    }
                    getStructureDefinition(id: "string") {
                        url
                        status
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFindResourceWhichIsOnlyDefinedByStructureDefinition() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(sort: [ { name: ASC } ]) {
                        id
                        name
                        type {
                            coding {
                                system
                                code
                            }
                        }
                        address(sort: [ { city: ASC } ]) {
                            city
                            state
                            postalCode
                            country
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanSortByScalarFields() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(sort: [ { name: ASC } ]) {
                        id
                        name
                        type {
                            coding {
                                system
                                code
                            }
                        }
                        address(sort: [ { city: ASC } ]) {
                            city
                            state
                            postalCode
                            country
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanPaginate() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(sort: [ { name: ASC } ], pagination: { offset: 0, limit: 2 }) {
                        id
                        name
                        type {
                            coding {
                                system
                                code
                            }
                        }
                        address {
                            city
                            state
                            postalCode
                            country
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanSortByObjectFields() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(sort: [ { address: { city: DESC } } ]) {
                        id
                        name
                        type {
                            coding {
                                system
                                code
                            }
                        }
                        address(sort: [ { city: ASC } ]) {
                            city
                            state
                            postalCode
                            country
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanSortByEventDeeperObjectField() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(sort: [ { type: {coding: { code: DESC } } }, { name: DESC } ]) {
                        id
                        name
                        type {
                            coding {
                                system
                                code
                            }
                        }
                        address(sort: [ { city: ASC } ]) {
                            city
                            state
                            postalCode
                            country
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itWillApplySortSpecifiedOnInnerFieldWhenSortingByObjectField() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(sort: [ { address: { city: DESC } } ]) {
                        id
                        name
                        type {
                            coding {
                                system
                                code
                            }
                        }
                        address(sort: [ { city: DESC } ] ) {
                            city
                            state
                            postalCode
                            country
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFilterByScalarFields() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(filter: [ { name_STARTS_WITH: "L" } ]) {
                        id
                        name
                        type {
                            coding {
                                system
                                code
                            }
                        }
                        address(filter: [ { city_ENDS_WITH: "c" } ]) {
                            city
                            state
                            postalCode
                            country
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFilterByListScalarFields() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(sort: [ { name: ASC } ]) {
                        name
                        address(filter: [ { line_ANY_MATCH: { EQUALS: "Dlouhá 21" } } ]) {
                            city
                            line
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFilterByCompositeFilter() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(
                        filter: [ {
                            OR: [
                                { name_STARTS_WITH: "L" },
                                { name_STARTS_WITH: "P" }
                            ]
                        } ],
                        sort: [ { name: ASC } ]
                    ) {
                        id
                        name
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFilterByNotFilter() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(
                        filter: [ {
                            NOT: { name_STARTS_WITH: "L" }
                        } ],
                        sort: [ { name: ASC } ]
                    ) {
                        id
                        name
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFilterByDeepObjectFields() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(sort: [ { name: ASC } ]) {
                        name
                        address(filter: [ { position: { latitude_GREATER_THAN: 50.1 } } ]) {
                            city
                            position {
                                latitude
                                longitude
                            }
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFilterByDeepListFields() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                    findOrganizationList(filter: [ { address_ALL_MATCH: { line_ANY_MATCH: {ENDS_WITH: "1"} } } ]) {
                        name
                        address(sort: [ { city: ASC } ]) {
                            city
                            line
                        }
                    }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanFilterVeryDeeply() {
    var graphQlRequest = new GraphQlOperation(
        "",
        """
                query {
                  findStructureDefinitionList(
                    filter: [
                      {
                        differential: {
                          element_ANY_MATCH: {
                            type_ANY_MATCH: {
                              code_EQUALS: "Duration"
                            }
                          }
                        }
                      }
                    ],
                    sort: [ { type: ASC } ]
                  ) {
                    type
                    kind
                    differential {
                      element(filter: [ { type_ANY_MATCH: { code_EQUALS: "Duration"} } ], sort: [ { path: ASC } ]) {
                        path
                        type(sort: [ { code: ASC } ]) {
                          code
                        }
                      }
                    }
                  }
                }
            """
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

  @Test
  void itCanResolveVariables() {
    var graphQlRequest = new GraphQlOperation(
        "ExampleOperation",
        """
                query ExampleOperation($structureDefinitionId: String!) {
                    getStructureDefinition(id: $structureDefinitionId) {
                        id
                        kind
                        abstract
                    }
                }
            """,
        new HashMap<>(Map.of(
            "structureDefinitionId", "CodeableConcept"
        ))
    );

    var response = this.graphQlExecutor.execute(graphQlRequest);
    this.thenObjectApproved(response);
  }

//    @Test
//    public void itCanGetStructureDefinitionAndItsElements() {
//        var graphQlRequest = new GraphQLQuery(
//            "",
//            """
//                    query {
//                        getStructureDefinition(id: "Patient") {
//                            id
//                            kind
//                            abstract
//                            differential {
//                                element(
//                                    pagination: {limit: 100, offset: 0},
//                                    sort: [
//                                        { path: ASC },
//                                        {
//                                            complexUnion: {
//                                                onComplex1: {uglyName: ASC},
//                                                onComplex2: {prettyName: ASC },
//                                                onBoxedString: {value: ASC }
//                                            }
//                                        }
//                                    ]
//                                ) {
//                                    id
//                                    path
//                                    min
//                                    max
//                                    complexUnion {
//                                        ... on Complex1 {
//                                            uglyName
//                                        }
//                                        ... on Complex2 {
//                                            prettyName
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                """
//        );
//
//        var response = this.whenQueryIsDispatched(graphQlRequest, LinkedHashMap.class);
//        this.thenObjectApproved(response);
//    }
}