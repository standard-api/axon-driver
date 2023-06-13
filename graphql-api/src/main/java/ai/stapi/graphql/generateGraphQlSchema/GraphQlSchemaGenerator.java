package ai.stapi.graphql.generateGraphQlSchema;

import ai.stapi.schema.structureSchema.ComplexStructureType;
import ai.stapi.schema.structureSchema.StructureSchema;
import java.util.List;

public interface GraphQlSchemaGenerator {

  String generate(
      StructureSchema graphDefinition,
      List<ComplexStructureType> operationDefinitions
  );

}
