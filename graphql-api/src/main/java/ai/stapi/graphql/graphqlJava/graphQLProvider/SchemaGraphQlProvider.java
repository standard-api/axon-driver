package ai.stapi.graphql.graphqlJava.graphQLProvider;

import ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator.GraphQlJavaSchemaGenerator;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionStructureTypeMapper;
import ai.stapi.schema.scopeProvider.ScopeCacher;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaProvider;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import java.util.Timer;
import java.util.TimerTask;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class SchemaGraphQlProvider implements GraphQLProvider {

  private final GraphQlJavaSchemaGenerator graphQlJavaSchemaGenerator;
  private final StructureSchemaProvider structureSchemaProvider;
  private final OperationDefinitionProvider operationDefinitionProvider;
  private final OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper;
  private final Logger logger;
  private final ScopeCacher scopeCacher;
  private final Timer initializeGraphQlTimer = new Timer();
  private TimerTask initializeGraphQlTask;

  public SchemaGraphQlProvider(
      GraphQlJavaSchemaGenerator graphQlJavaSchemaGenerator,
      StructureSchemaProvider structureSchemaProvider,
      OperationDefinitionProvider operationDefinitionProvider,
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper,
      ScopeCacher scopeCacher
  ) {
    this.graphQlJavaSchemaGenerator = graphQlJavaSchemaGenerator;
    this.structureSchemaProvider = structureSchemaProvider;
    this.operationDefinitionProvider = operationDefinitionProvider;
    this.operationDefinitionStructureTypeMapper = operationDefinitionStructureTypeMapper;
    this.scopeCacher = scopeCacher;
    this.logger = LoggerFactory.getLogger(SchemaGraphQlProvider.class);
  }

  @Override
  public void initialize() {
    this.scopeCacher.cache(
        SchemaGraphQlProvider.class,
        this.build()
    );
  }

  @Override
  public void reinitialize() {
    if (this.initializeGraphQlTask != null) {
      this.initializeGraphQlTask.cancel();
    }
    var dis = this;
    this.initializeGraphQlTask = new TimerTask() {
      @Override
      public void run() {
        dis.initialize();
      }
    };
    this.initializeGraphQlTimer.schedule(this.initializeGraphQlTask, 5000);
  }

  @Override
  public boolean isInitialized() {
    return this.scopeCacher.hasCached(SchemaGraphQlProvider.class);
  }

  @Override
  @Nullable
  public GraphQL getGraphQL() {
    return this.scopeCacher.getCachedOrCompute(
        SchemaGraphQlProvider.class,
        scope -> this.build()
    ).getGraphQL();
  }

  @Override
  @Nullable
  public GraphQLSchema getGraphQLSchema() {
    return this.scopeCacher.getCachedOrCompute(
        SchemaGraphQlProvider.class,
        scope -> this.build()
    ).getGraphQLSchema();
  }

  private SchemaAndGraphQl build() {
    var graphDefinition = this.structureSchemaProvider.provideSchema();
    if (graphDefinition.getStructureTypes().size() == 0) {
      this.logger.warn(
          "There was no StructureTypes in GraphDefinition to be found when initializing GraphQL. " +
              "Therefore GraphQL service and related endpoint will not work."
      );
    }
    var operationDefinitions = this.operationDefinitionProvider.provideAll();
    if (operationDefinitions.isEmpty()) {
      this.logger.warn(
          "There was no Operation Definitions to be found when starting Application. " +
              "Therefore GraphQL mutations will not work."
      );
    }

    var graphQLSchema = this.graphQlJavaSchemaGenerator.generateSchema(
        graphDefinition,
        this.operationDefinitionStructureTypeMapper.map(operationDefinitions)
    );
    var graphQl = GraphQL.newGraphQL(graphQLSchema).build();
    return new SchemaAndGraphQl(graphQLSchema, graphQl);
  }
  
  private static class SchemaAndGraphQl {
    private final GraphQLSchema graphQLSchema;
    private final GraphQL graphQL;

    public SchemaAndGraphQl(GraphQLSchema graphQLSchema, GraphQL graphQL) {
      this.graphQLSchema = graphQLSchema;
      this.graphQL = graphQL;
    }

    public GraphQLSchema getGraphQLSchema() {
      return graphQLSchema;
    }

    public GraphQL getGraphQL() {
      return graphQL;
    }
  }
}
