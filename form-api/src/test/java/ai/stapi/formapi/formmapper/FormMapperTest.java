package ai.stapi.formapi.formmapper;

import static org.junit.jupiter.api.Assertions.*;

import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaProvider;
import ai.stapi.test.domain.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import ai.stapi.formapi.formmapper.fixtures.FormApiTestDefinitionsLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@StructureDefinitionScope(FormApiTestDefinitionsLoader.SCOPE)
class FormMapperTest extends DomainTestCase {
  
  @Autowired
  private OperationDefinitionProvider operationDefinitionProvider;
  
  @Autowired
  private StructureSchemaProvider structureSchemaProvider;

  @Test
  void itShouldDoSomething() {
    var operationDefinitions = operationDefinitionProvider.provideAll();
    
    var operationDefinition = this.operationDefinitionProvider.provide("TestSimpleCommand");
    fail("Not yet implemented");
  }

}