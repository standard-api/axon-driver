package ai.stapi.graphql.graphqlJava.testfixtures;

import ai.stapi.schema.adHocLoaders.AbstractFileModelDefinitionsLoader;
import ai.stapi.schema.adHocLoaders.FileLoader;
import ai.stapi.schema.scopeProvider.ScopeOptions;
import org.springframework.stereotype.Service;

@Service
public class TestGraphqlModelDefinitionsLoader extends AbstractFileModelDefinitionsLoader {

  public static final String SCOPE = "test-graphql";
  public static final String TAG = ScopeOptions.TEST_TAG;

  public TestGraphqlModelDefinitionsLoader(
      FileLoader fileLoader
  ) {
    super(fileLoader, SCOPE, TAG);
  }
}
