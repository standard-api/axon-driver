package ai.stapi.test.systemschema;

import ai.stapi.graphsystem.systemfixtures.model.SystemModelDefinitionsLoader;
import ai.stapi.test.schemaintegration.SchemaIntegrationTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;

@StructureDefinitionScope(SystemModelDefinitionsLoader.SCOPE)
public class SystemSchemaIntegrationTestCase extends SchemaIntegrationTestCase {
}
