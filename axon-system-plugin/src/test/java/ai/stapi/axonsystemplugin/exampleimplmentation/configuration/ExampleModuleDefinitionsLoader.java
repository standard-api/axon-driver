package ai.stapi.axonsystemplugin.exampleimplmentation.configuration;

import ai.stapi.schema.adHocLoaders.AbstractFileModelDefinitionsLoader;
import ai.stapi.schema.adHocLoaders.FileLoader;
import ai.stapi.schema.scopeProvider.ScopeOptions;
import org.springframework.stereotype.Service;

@Service
public class ExampleModuleDefinitionsLoader extends AbstractFileModelDefinitionsLoader {

  public static final String SCOPE = "ExampleModule";
  public static final String TAG = ScopeOptions.TEST_TAG;

  public ExampleModuleDefinitionsLoader(
      FileLoader fileLoader
  ) {
    super(fileLoader, SCOPE, TAG);
  }
}
