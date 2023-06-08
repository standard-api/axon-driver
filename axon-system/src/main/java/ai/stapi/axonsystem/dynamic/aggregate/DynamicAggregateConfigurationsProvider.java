package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.axonsystem.dynamic.aggregate.exceptions.CannotProvideDynamicAggregateConfiguration;
import java.util.List;

public interface DynamicAggregateConfigurationsProvider {

  List<DynamicAggregateConfiguration> provide();

  DynamicAggregateConfiguration provide(String aggregateType)
      throws CannotProvideDynamicAggregateConfiguration;
}
