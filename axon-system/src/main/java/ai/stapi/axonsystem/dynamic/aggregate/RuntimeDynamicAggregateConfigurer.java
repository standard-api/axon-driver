package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.axonsystem.dynamic.aggregate.exceptions.CannotProvideDynamicAggregateConfiguration;
import ai.stapi.schema.structureSchema.exception.StructureSchemaException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.axonframework.config.Configurer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuntimeDynamicAggregateConfigurer {

  private final DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider;
  private final Configurer configurer;
  private final Map<String, List<String>> failedAddedAggregateDefinitions;
  private final Logger logger;

  public RuntimeDynamicAggregateConfigurer(
      DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider,
      Configurer configurer
  ) {
    this.dynamicAggregateConfigurationsProvider = dynamicAggregateConfigurationsProvider;
    this.configurer = configurer;
    this.logger = LoggerFactory.getLogger(RuntimeDynamicAggregateConfigurer.class);
    this.failedAddedAggregateDefinitions = new HashMap<>();
  }

  public void add(String aggregateType) {
    DynamicAggregateConfiguration aggregateDefinition;
    try {
      aggregateDefinition = this.dynamicAggregateConfigurationsProvider.provide(aggregateType);
    } catch (CannotProvideDynamicAggregateConfiguration e) {
      throw new RuntimeException("Cannot Configure Dynamic Aggregate at runtime.", e);
    }
    this.add(aggregateDefinition);
  }

  public void add(DynamicAggregateConfiguration aggregateDefinition) {
    var aggregateType = aggregateDefinition.getAggregateType();
    try {
      aggregateDefinition.setAsAddedAtRuntime();
      this.configurer.configureAggregate(aggregateDefinition);
      var message = "New aggregate added at runtime: [%s]".formatted(aggregateType);
      this.logger.info(message);
      this.tryToConfigureFailed(aggregateType);
    } catch (StructureSchemaException exception) {
      var missingSerializationType = exception.getParentDefinitionType();
      this.addToFailedTypes(aggregateType, missingSerializationType);
    }
  }

  public void tryToConfigureFailed(String successfullyAddedType) {
    if (this.failedAddedAggregateDefinitions.containsKey(successfullyAddedType)) {
      var previouslyFailed = this.failedAddedAggregateDefinitions.get(successfullyAddedType);
      this.failedAddedAggregateDefinitions.put(successfullyAddedType, new ArrayList<>());
      previouslyFailed.forEach(this::add);
    }
  }

  public Map<String, List<String>> getFailedAddedAggregateDefinitions() {
    return failedAddedAggregateDefinitions;
  }

  private void addToFailedTypes(String aggregateType, String missingType) {
    this.failedAddedAggregateDefinitions.computeIfAbsent(
        missingType,
        key -> new ArrayList<>()
    ).add(aggregateType);
  }
}
