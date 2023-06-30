package ai.stapi.axonsystem.dynamic.aggregate;

import java.util.ArrayList;
import java.util.List;
import org.axonframework.config.Configurer;
import org.axonframework.config.ConfigurerModule;
import org.jetbrains.annotations.NotNull;

public class DynamicAggregateConfigurer implements ConfigurerModule {

  private final DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider;
  private final RuntimeDynamicAggregateConfigurer runtimeDynamicAggregateConfigurer;
  private final List<String> configuredAggregates;

  public DynamicAggregateConfigurer(
      DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider,
      RuntimeDynamicAggregateConfigurer runtimeDynamicAggregateConfigurer
  ) {
    this.dynamicAggregateConfigurationsProvider = dynamicAggregateConfigurationsProvider;
    this.runtimeDynamicAggregateConfigurer = runtimeDynamicAggregateConfigurer;
    this.configuredAggregates = new ArrayList<>();
  }

  @Override
  public void configureModule(@NotNull Configurer configurer) {
    var aggregateConfigurations = this.dynamicAggregateConfigurationsProvider.provide();
    aggregateConfigurations.forEach(configuration -> {
      configurer.configureAggregate(configuration);
      var aggregateClass = configuration.aggregateType();
      if (aggregateClass.equals(DynamicAggregate.class)) {
        this.configuredAggregates.add(configuration.getAggregateType());
      } else {
        this.configuredAggregates.add(aggregateClass.getCanonicalName());
      }
    });
  }

  public void configureNewAggregates() {
    var currentConfigs = this.dynamicAggregateConfigurationsProvider.provide();
    currentConfigs.stream()
        .filter(config -> !this.configuredAggregates.contains(config.getAggregateType()))
        .forEach(this.runtimeDynamicAggregateConfigurer::add);
  }
}
