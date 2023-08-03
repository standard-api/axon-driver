package ai.stapi.formapi.configuration;

import ai.stapi.formapi.forminfo.FormInfoMapper;
import ai.stapi.formapi.formmapper.*;
import ai.stapi.graphoperations.dagtoobjectconverter.DAGToObjectConverter;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregategraphstatemodifier.EventFactoryModificationTraverser;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionProvider;
import ai.stapi.graphsystem.operationdefinition.model.OperationDefinitionStructureTypeMapper;
import ai.stapi.schema.structureSchemaProvider.StructureSchemaFinder;
import org.axonframework.config.Configuration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan("ai.stapi.formapi")
public class FormApiConfiguration {
  
  @Bean
  public FormMapper formMapper(
      JsonSchemaMapper jsonSchemaMapper,
      UISchemaLoader uiSchemaLoader,
      FormDataLoader formDataLoader,
      OperationDefinitionProvider operationDefinitionProvider
  ) {
    return new FormMapper(
        jsonSchemaMapper,
        uiSchemaLoader,
        formDataLoader,
        operationDefinitionProvider
    );
  }

  @Bean
  public JsonSchemaMapper jsonSchemaMapper(
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper,
      StructureSchemaFinder structureSchemaFinder
  ) {
    return new JsonSchemaMapper(operationDefinitionStructureTypeMapper, structureSchemaFinder);
  }

  @Bean
  @ConditionalOnMissingBean(UISchemaLoader.class)
  public UISchemaLoader schemaLoader() {
    return new NullUISchemaLoader();
  }

  @Bean
  @ConditionalOnMissingBean(FormDataLoader.class)
  public AggregateRepositoryFormDataLoader formDataLoader(
      Configuration configuration,
      AggregateDefinitionProvider aggregateDefinitionProvider,
      EventFactoryModificationTraverser eventFactoryModificationTraverser,
      OperationDefinitionStructureTypeMapper operationDefinitionStructureTypeMapper,
      DAGToObjectConverter dagToObjectConverter
  ) {
    return new AggregateRepositoryFormDataLoader(
        configuration, 
        aggregateDefinitionProvider, 
        eventFactoryModificationTraverser,
        operationDefinitionStructureTypeMapper,
        dagToObjectConverter
    );
  }

  @Bean
  public FormInfoMapper formInfoMapper(
      AggregateDefinitionProvider aggregateDefinitionProvider,
      OperationDefinitionProvider operationDefinitionProvider,
      StructureSchemaFinder structureSchemaFinder
  ) {
    return new FormInfoMapper(
        aggregateDefinitionProvider,
        operationDefinitionProvider,
        structureSchemaFinder
    );
  }
}
