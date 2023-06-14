package ai.stapi.graphql.graphqlJava.graphQlSchemaGenerator;

import ai.stapi.schema.structureSchema.PrimitiveStructureType;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import graphql.schema.GraphQLScalarType;
import org.jetbrains.annotations.NotNull;

public class GraphQlScalarSchemaGenerator {

  public GraphQLScalarType generateScalarType(PrimitiveStructureType primitiveStructureType) {
    return new GraphQLScalarType.Builder()
        .name(primitiveStructureType.getDefinitionType())
        .description(primitiveStructureType.getDescription())
        .coercing(new Coercing<>() {
          @Override
          public Object serialize(@NotNull Object dataFetcherResult)
              throws CoercingSerializeException {
            return dataFetcherResult;
          }

          @Override
          public @NotNull Object parseValue(@NotNull Object input)
              throws CoercingParseValueException {
            return input;
          }

          @Override
          public @NotNull Object parseLiteral(@NotNull Object input)
              throws CoercingParseLiteralException {
            return input;
          }
        })
        .build();
  }
}
