package ai.stapi.axonsystemplugin.fixtures;

import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import ai.stapi.identity.UniqueIdentifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GenerateFixtures extends AbstractCommand<UniqueIdentifier> {

  public static final String SERIALIZATION_TYPE = "GenerateFixtures";
  public static final String TARGET_IDENTIFIER = Fixtures.TARGET_AGGREGATE_IDENTIFIER;

  private List<String> fixtureTags;

  private float minPriority = 0;

  private float maxPriority = 1000000;

  private GenerateFixtures() {
    super(new UniqueIdentifier(TARGET_IDENTIFIER), SERIALIZATION_TYPE);
  }

  public GenerateFixtures(String... fixtureTags) {
    super(new UniqueIdentifier(TARGET_IDENTIFIER), SERIALIZATION_TYPE);
    this.fixtureTags = Arrays.stream(fixtureTags).collect(Collectors.toList());
  }

  public GenerateFixtures(
      List<String> fixtureTags,
      float minPriority,
      float maxPriority
  ) {
    super(new UniqueIdentifier(TARGET_IDENTIFIER), SERIALIZATION_TYPE);
    this.fixtureTags = new ArrayList<>(fixtureTags);
    this.minPriority = minPriority;
    this.maxPriority = maxPriority;
  }

  public List<String> getFixtureTags() {
    return fixtureTags;
  }

  public float getMinPriority() {
    return minPriority;
  }

  public float getMaxPriority() {
    return maxPriority;
  }
}
