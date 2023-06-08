package ai.stapi.axonsystemplugin.fixtures;

import ai.stapi.graphsystem.messaging.event.Event;
import java.util.Set;

public class FixtureGenerated implements Event {

  private String generatorClassName;
  private Set<String> fixtureFileNames;

  private boolean isOneTimeGenerator;

  private FixtureGenerated() {
  }

  public FixtureGenerated(
      String generatorClassName,
      Set<String> fixtureFileNames,
      boolean isOneTimeGenerator
  ) {
    this.generatorClassName = generatorClassName;
    this.fixtureFileNames = fixtureFileNames;
    this.isOneTimeGenerator = isOneTimeGenerator;
  }

  public String getGeneratorClassName() {
    return generatorClassName;
  }

  public Set<String> getFixtureFileNames() {
    return fixtureFileNames;
  }

  public boolean isOneTimeGenerator() {
    return isOneTimeGenerator;
  }
}
