package ai.stapi.graphql.graphqlJava.exceptions;

public class CannotGenerateGraphQLSchema extends RuntimeException {

  private CannotGenerateGraphQLSchema(String becauseMessage) {
    super("Cannot generate graphQl schema, because " + becauseMessage);
  }

  public static CannotGenerateGraphQLSchema becauseStructureSchemaFieldHadNoType(
      String typeName,
      String fieldName
  ) {
    return new CannotGenerateGraphQLSchema(
        "structure schema has some field which has no type." +
            "\nType name: " + typeName +
            "\nField name: " + fieldName
    );
  }

  public static CannotGenerateGraphQLSchema becauseOperationReferencedTypeWhichDoesNotExistInStructureSchema(
      String operationName,
      String parameterTypeName
  ) {
    return new CannotGenerateGraphQLSchema(
        "operation has parameter of type which does not exist in structure schema." +
            "\nOperation name: " + operationName +
            "\nParameter Type name: " + parameterTypeName
    );
  }
}
