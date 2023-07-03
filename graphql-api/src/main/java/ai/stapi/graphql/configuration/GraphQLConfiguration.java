package ai.stapi.graphql.configuration;

import ai.stapi.axonsystem.configuration.CommandGatewayConfiguration;
import ai.stapi.graphoperations.graphLoader.GraphLoader;
import ai.stapi.graphoperations.graphLoader.search.filterOption.factory.FilterOptionFactory;
import ai.stapi.graphql.GraphQlExecutor;
import ai.stapi.graphql.InitializeGraphQlCommandLineRunner;
import ai.stapi.graphql.PrintGraphQLCommandLineRunner;
import ai.stapi.graphql.generateGraphQlSchema.GraphQlSchemaGenerator;
import ai.stapi.graphql.generateGraphQlSchema.PrintGraphQlSchemaCommandHandler;
import ai.stapi.graphql.graphqlJava.CommandGqlDataFetcher;
import ai.stapi.graphql.graphqlJava.GraphLoaderGqlDataFetcher;
import ai.stapi.graphql.graphqlJava.graphQLProvider.GraphQLProvider;
import ai.stapi.graphql.graphqlJava.graphQLProvider.SchemaGraphQlProvider;
import ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator.GraphQlFilterInputGenerator;
import ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator.GraphQlJavaSchemaGenerator;
import ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator.GraphQlObjectTypeGenerator;
import ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator.GraphQlScalarSchemaGenerator;
import ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator.GraphQlSortInputGenerator;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionStructureTypeMapper;
import ai.stapi.schema.scopeProvider.ScopeCacher;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaProvider;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.axonframework.springboot.autoconfig.AxonServerAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

@AutoConfiguration
@ComponentScan("ai.stapi.graphql")
@AutoConfigureAfter({CommandGatewayConfiguration.class, AxonAutoConfiguration.class, AxonServerAutoConfiguration.class})
public class GraphQLConfiguration {

  @Bean
  public GraphQlExecutor graphQlExecutor(GraphQLProvider graphQLProvider) {
    return new GraphQlExecutor(graphQLProvider);
  }

  @Bean
  @ConditionalOnMissingBean(GraphQLProvider.class)
  public SchemaGraphQlProvider schemaGraphQlProvider(
      GraphQlJavaSchemaGenerator graphQlJavaSchemaGenerator,
      StructureSchemaProvider structureSchemaProvider,
      OperationDefinitionProvider operationDefinitionProvider,
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper,
      ScopeCacher scopeCacher
  ) {
    return new SchemaGraphQlProvider(
        graphQlJavaSchemaGenerator,
        structureSchemaProvider,
        operationDefinitionProvider,
        operationDefinitionStructureTypeMapper,
        scopeCacher
    );
  }
  
  @Bean
  @ConditionalOnMissingBean(GraphQlSchemaGenerator.class)
  @ConditionalOnBean(SchemaGraphQlProvider.class)
  public GraphQlJavaSchemaGenerator graphQlJavaSchemaGenerator(
      GraphQlSortInputGenerator graphQlSortInputGenerator,
      GraphQlFilterInputGenerator graphQlFilterInputGenerator,
      GraphQlObjectTypeGenerator graphQlObjectTypeGenerator,
      GraphQlScalarSchemaGenerator graphQlScalarSchemaGenerator,
      GraphLoaderGqlDataFetcher graphLoaderGqlDataFetcher,
      CommandGqlDataFetcher commandGqlDataFetcher
  ) {
    return new GraphQlJavaSchemaGenerator(
        graphQlSortInputGenerator,
        graphQlFilterInputGenerator,
        graphQlObjectTypeGenerator,
        graphQlScalarSchemaGenerator,
        graphLoaderGqlDataFetcher,
        commandGqlDataFetcher
    );
  }
  
  @Bean
  @ConditionalOnBean(GraphQlJavaSchemaGenerator.class)
  public GraphQlSortInputGenerator graphQlSortInputGenerator() {
    return new GraphQlSortInputGenerator();
  }

  @Bean
  @ConditionalOnBean(GraphQlJavaSchemaGenerator.class)
  public GraphQlFilterInputGenerator graphQlFilterInputGenerator() {
    return new GraphQlFilterInputGenerator();
  }

  @Bean
  @ConditionalOnBean(GraphQlJavaSchemaGenerator.class)
  public GraphQlObjectTypeGenerator graphQlObjectTypeGenerator(
      GraphQlSortInputGenerator graphQLSortInputGenerator,
      GraphQlFilterInputGenerator graphQlFilterInputGenerator
  ) {
    return new GraphQlObjectTypeGenerator(
        graphQLSortInputGenerator,
        graphQlFilterInputGenerator
    );
  }

  @Bean
  @ConditionalOnBean(GraphQlJavaSchemaGenerator.class)
  public GraphQlScalarSchemaGenerator graphQlScalarSchemaGenerator() {
    return new GraphQlScalarSchemaGenerator();
  }

  @Bean
  @ConditionalOnBean(GraphQlJavaSchemaGenerator.class)
  public GraphLoaderGqlDataFetcher graphLoaderGqlDataFetcher(
      GraphLoader graphLoader,
      FilterOptionFactory filterOptionFactory
  ) {
    return new GraphLoaderGqlDataFetcher(graphLoader, filterOptionFactory);
  }

  @Bean
  @ConditionalOnBean(GraphQlJavaSchemaGenerator.class)
  public CommandGqlDataFetcher commandGqlDataFetcher(
      CommandGateway commandGateway,
      AggregateDefinitionProvider aggregateDefinitionProvider,
      StructureSchemaProvider graphDefinitionProvider,
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper
  ) {
    return new CommandGqlDataFetcher(
        commandGateway, 
        aggregateDefinitionProvider, 
        graphDefinitionProvider, 
        operationDefinitionStructureTypeMapper
    );
  }
  
  @Bean
  public InitializeGraphQlCommandLineRunner initializeGraphQlCommandLineRunner(GraphQLProvider graphQLProvider) {
    return new InitializeGraphQlCommandLineRunner(graphQLProvider);
  }
  
  @Bean
  public PrintGraphQlSchemaCommandHandler printGraphQlSchemaCommandHandler(
      GraphQlSchemaGenerator graphQlSchemaGenerator,
      StructureSchemaProvider graphDefinitionProvider,
      OperationDefinitionProvider operationDefinitionProvider,
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper
  ) {
    return new PrintGraphQlSchemaCommandHandler(
        graphQlSchemaGenerator,
        graphDefinitionProvider,
        operationDefinitionProvider,
        operationDefinitionStructureTypeMapper
    );
  }
  
  @Bean
  @Profile("print-graphql-schema")
  public PrintGraphQLCommandLineRunner printGraphQLCommandLineRunner(
      CommandGateway commandGateway,
      ApplicationContext applicationContext
  ) {
    return new PrintGraphQLCommandLineRunner(commandGateway, applicationContext);
  }
}
