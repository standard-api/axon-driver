package ai.stapi.graphql.graphqlJava.exceptions;

import ai.stapi.schema.structureSchemaProvider.exception.CannotProvideStructureSchema;
import graphql.language.ObjectValue;
import graphql.language.Value;
import graphql.schema.GraphQLFieldDefinition;
import java.util.List;

public class CannotLoadRequestedDataByGraphQL extends RuntimeException {

  private CannotLoadRequestedDataByGraphQL(String becauseMessage) {
    super("Cannot load requested data by GraphQL, because " + becauseMessage);
  }

  private CannotLoadRequestedDataByGraphQL(String becauseMessage, Throwable e) {
    super("Cannot load requested data by GraphQL, because " + becauseMessage, e);
  }

  public static CannotLoadRequestedDataByGraphQL becauseInvalidFieldInQuery(
      GraphQLFieldDefinition fieldDefinition) {
    return new CannotLoadRequestedDataByGraphQL(
        "invalid field encountered in main Query object." +
            "\nField should either return list and take search query parameters. Or return object and take string id parameter."
            +
            "\nGQL field definition: " + fieldDefinition.toString()
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseInnerFieldWasNotObjectOrScalar(
      GraphQLFieldDefinition fieldDefinition) {
    return new CannotLoadRequestedDataByGraphQL(
        "some inner field definition was not Object or Scalar." +
            "\nDefinition: " + fieldDefinition.toString()
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedSortOptionDidNotHaveExactlyOneField(
      ObjectValue sortOptionValue) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided sort option did not have exactly one field." +
            "\nSort option: " + sortOptionValue.toString()
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedFilterOptionDidNotHaveExactlyOneField(
      ObjectValue filterOptionValue) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided filter option did not have exactly one field." +
            "\nFilter option: " + filterOptionValue.toString()
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedSortOptionWasOfUnknownType(
      Value<?> value) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided sort option was of unknown type." +
            "\nSort value: " + value.toString()
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedFilterOptionWasOfUnknownStrategy(
      String filterStrategy) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided filter option was of unknown strategy." +
            "\nStrategy: " + filterStrategy
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedListFilterInputWasNotOfObjectValue(
      String filterStrategy,
      String attributeName,
      Value<?> value
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided list filter input was not of object value." +
            "\nActual value: " + value +
            "\nStrategy: " + filterStrategy +
            "\nAttribute name: " + attributeName
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedMultiLogicalFilterInputWasNotOfArrayValue(
      String filterStrategy,
      Value<?> value
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided multi logical filter input was not of array value." +
            "\nActual value: " + value +
            "\nStrategy: " + filterStrategy
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedLogicalFilterInputHadInvalidType(
      String filterStrategy,
      List<Value> invalidValueItems
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided logical filter input was not object value." +
            "\nInvalid values: " + invalidValueItems +
            "\nStrategy: " + filterStrategy
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedDeepFilterWasNotOfObjectValue(
      String edgeType,
      Value<?> value
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided deep filter input was not of object value." +
            "\nActual value: " + value +
            "\nEdge type/Field name: " + edgeType
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedMutationDidNotHaveCorrespondingOperationDefinition(
      String commandName
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided mutation did not have corresponding operation definition." +
            "\nCommand name: " + commandName
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedMutationHadParameterOfUnknownType(
      String commandName,
      String parameterName,
      String parameterType
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided mutation had parameter of unknown type." +
            "\nCommand name: " + commandName +
            "\nParameter name: " + parameterName +
            "\nParameter type: " + parameterType
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedMutationHadParameterOfComplexTypeWithSomeFieldOfUnknownType(
      String commandName,
      String complexTypeName,
      String fieldName,
      String fieldType
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided mutation had parameter of complex type with some field of unknown type." +
            "\nCommand name: " + commandName +
            "\nType name: " + complexTypeName +
            "\nField name: " + fieldName +
            "\nField type: " + fieldType
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedMutationHadParameterOfComplexTypeWithSomeInvalidUnionValue(
      String commandName,
      String typeName,
      String fieldName,
      Object actualUnionValue
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided mutation had parameter of complex type with some field of invalid union type." +
            "\nCommand name: " + commandName +
            "\nType name: " + typeName +
            "\nField name: " + fieldName +
            "\nUnion value: " + actualUnionValue
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseUnionInputDidNotHaveExactlyOneMemberSpecified(
      String commandName,
      String typeName,
      String fieldName,
      Object actualUnionValue
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided mutation had parameter of complex type with some invalid field of union type." +
            "\nThe value of this type did not have exactly one union member specified." +
            "\nCommand name: " + commandName +
            "\nType name: " + typeName +
            "\nField name: " + fieldName +
            "\nUnion value: " + actualUnionValue
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedMutationHadParameterWhichDoesNotHaveStructureSchema(
      String commandName,
      String fieldName,
      String fieldType,
      CannotProvideStructureSchema e
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided mutation had parameter which does not have Structure Schema." +
            "\nCommand name: " + commandName +
            "\nParameter name: " + fieldName +
            "\nParameter type: " + fieldType,
        e
    );
  }

  public static CannotLoadRequestedDataByGraphQL becauseProvidedMutationHadParameterOfComplexTypeWithSomeFieldWhichDoesNotHaveStructureSchema(
      String commandName,
      String definitionType,
      String fieldName,
      String fieldType,
      CannotProvideStructureSchema e
  ) {
    return new CannotLoadRequestedDataByGraphQL(
        "provided mutation had parameter of complex type with some field which does not have Structure Schema."
            +
            "\nCommand name: " + commandName +
            "\nType name: " + definitionType +
            "\nField name: " + fieldName +
            "\nField type: " + fieldType,
        e
    );
  }
}
