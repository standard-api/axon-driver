package ai.stapi.graphql.generateGraphQlSchema;

import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionStructureTypeMapper;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaProvider;
import java.io.FileWriter;
import java.io.IOException;
import org.axonframework.commandhandling.CommandHandler;

public class PrintGraphQlSchemaCommandHandler {

  private final GraphQlSchemaGenerator graphQlSchemaGenerator;
  private final StructureSchemaProvider graphDefinitionProvider;
  private final OperationDefinitionProvider operationDefinitionProvider;
  private final OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper;

  public PrintGraphQlSchemaCommandHandler(
      GraphQlSchemaGenerator graphQlSchemaGenerator,
      StructureSchemaProvider graphDefinitionProvider,
      OperationDefinitionProvider operationDefinitionProvider,
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper
  ) {
    this.graphQlSchemaGenerator = graphQlSchemaGenerator;
    this.graphDefinitionProvider = graphDefinitionProvider;
    this.operationDefinitionProvider = operationDefinitionProvider;
    this.operationDefinitionStructureTypeMapper = operationDefinitionStructureTypeMapper;
  }

  @CommandHandler
  public void handle(PrintGraphQlSchema command) throws IOException {
    var graphDefinition = this.graphDefinitionProvider.provideSchema();
    var operationDefinitions = this.operationDefinitionProvider.provideAll();
    var schema = this.graphQlSchemaGenerator.generate(
        graphDefinition,
        this.operationDefinitionStructureTypeMapper.map(operationDefinitions)
    );
    try (var writer = new FileWriter(command.getOutputPath())) {
      writer.write(schema);
    }
  }
}
