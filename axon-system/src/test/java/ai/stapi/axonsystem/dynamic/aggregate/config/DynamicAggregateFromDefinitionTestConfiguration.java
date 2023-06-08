package ai.stapi.axonsystem.dynamic.aggregate.config;

import ai.stapi.axonsystem.dynamic.aggregate.testImplementations.TestAggregateDefinitionProvider;
import ai.stapi.axonsystem.dynamic.aggregate.testImplementations.TestOperationDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;


public class DynamicAggregateFromDefinitionTestConfiguration {

  @Bean
  @Primary
  public AggregateDefinitionProvider testAggregateDefinitionProvider() {
    return new TestAggregateDefinitionProvider();
  }

  @Bean
  @Primary
  public OperationDefinitionProvider testOperationDefinitionProvider() {
    return new TestOperationDefinitionProvider();
  }
}
