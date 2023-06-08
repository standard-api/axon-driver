package ai.stapi.axonsystem.dynamic;

import ai.stapi.axonsystem.dynamic.aggregate.RuntimeDynamicAggregateConfigurer;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregate;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateConfiguration;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateConfigurationsProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.AggregateConfiguration;
import org.axonframework.config.Configuration;
import org.axonframework.config.Configurer;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.config.LifecycleHandler;
import org.axonframework.config.MessageMonitorFactory;
import org.axonframework.config.ModuleConfiguration;
import org.axonframework.config.TagsConfiguration;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.HandlerEnhancerDefinition;
import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.modelling.saga.ResourceInjector;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.upcasting.event.EventUpcaster;
import org.axonframework.spring.config.SpringConfigurer;
import org.jetbrains.annotations.NotNull;

public class DynamicAxonConfigurer implements Configurer {

  private final SpringConfigurer springConfigurer;
  private final DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider;
  private final RuntimeDynamicAggregateConfigurer runtimeDynamicAggregateConfigurer;
  private final List<String> configuredAggregates;

  public DynamicAxonConfigurer(
      SpringConfigurer springConfigurer,
      DynamicAggregateConfigurationsProvider dynamicAggregateConfigurationsProvider,
      RuntimeDynamicAggregateConfigurer runtimeDynamicAggregateConfigurer
  ) {
    this.springConfigurer = springConfigurer;
    this.dynamicAggregateConfigurationsProvider = dynamicAggregateConfigurationsProvider;
    this.runtimeDynamicAggregateConfigurer = runtimeDynamicAggregateConfigurer;
    this.configuredAggregates = new ArrayList<>();
  }

  @Override
  public Configurer registerEventUpcaster(
      @NotNull Function<Configuration, EventUpcaster> function) {
    return this.springConfigurer.registerEventUpcaster(function);
  }

  @Override
  public Configurer configureMessageMonitor(@NotNull
  Function<Configuration, BiFunction<Class<?>, String, MessageMonitor<Message<?>>>> function) {
    return this.springConfigurer.configureMessageMonitor(function);
  }

  @Override
  public Configurer configureMessageMonitor(@NotNull Class<?> componentType,
      @NotNull Function<Configuration, MessageMonitor<Message<?>>> messageMonitorBuilder) {
    return this.springConfigurer.configureMessageMonitor(componentType, messageMonitorBuilder);
  }

  @Override
  public Configurer configureMessageMonitor(@NotNull Class<?> aClass,
      @NotNull MessageMonitorFactory messageMonitorFactory) {
    return this.springConfigurer.configureMessageMonitor(aClass, messageMonitorFactory);
  }

  @Override
  public Configurer configureMessageMonitor(@NotNull Class<?> componentType,
      @NotNull String componentName,
      @NotNull Function<Configuration, MessageMonitor<Message<?>>> messageMonitorBuilder) {
    return this.springConfigurer.configureMessageMonitor(componentType, componentName,
        messageMonitorBuilder);
  }

  @Override
  public Configurer configureMessageMonitor(@NotNull Class<?> aClass, @NotNull String s,
      @NotNull MessageMonitorFactory messageMonitorFactory) {
    return this.springConfigurer.configureMessageMonitor(aClass, s, messageMonitorFactory);
  }

  @Override
  public Configurer configureCorrelationDataProviders(
      @NotNull Function<Configuration, List<CorrelationDataProvider>> function) {
    return this.springConfigurer.configureCorrelationDataProviders(function);
  }

  @Override
  public Configurer registerModule(@NotNull ModuleConfiguration moduleConfiguration) {
    return this.springConfigurer.registerModule(moduleConfiguration);
  }

  @Override
  public <C> Configurer registerComponent(@NotNull Class<C> aClass,
      @NotNull Function<Configuration, ? extends C> function) {
    return this.springConfigurer.registerComponent(aClass, function);
  }

  @Override
  public Configurer registerCommandHandler(@NotNull Function<Configuration, Object> function) {
    return this.springConfigurer.registerCommandHandler(function);
  }

  @Override
  @Deprecated
  public Configurer registerCommandHandler(int phase,
      @NotNull Function<Configuration, Object> commandHandlerBuilder) {
    return this.springConfigurer.registerCommandHandler(phase, commandHandlerBuilder);
  }

  @Override
  public Configurer registerQueryHandler(@NotNull Function<Configuration, Object> function) {
    return this.springConfigurer.registerQueryHandler(function);
  }

  @Override
  @Deprecated
  public Configurer registerQueryHandler(int phase,
      @NotNull Function<Configuration, Object> queryHandlerBuilder) {
    return this.springConfigurer.registerQueryHandler(phase, queryHandlerBuilder);
  }

  @Override
  public Configurer registerMessageHandler(@NotNull Function<Configuration, Object> function) {
    return this.springConfigurer.registerMessageHandler(function);
  }

  @Override
  public Configurer configureEmbeddedEventStore(
      @NotNull Function<Configuration, EventStorageEngine> function) {
    return this.springConfigurer.configureEmbeddedEventStore(function);
  }

  @Override
  public Configurer configureEventStore(
      @NotNull Function<Configuration, EventStore> eventStoreBuilder) {
    return this.springConfigurer.configureEventStore(eventStoreBuilder);
  }

  @Override
  public Configurer configureEventBus(@NotNull Function<Configuration, EventBus> eventBusBuilder) {
    return this.springConfigurer.configureEventBus(eventBusBuilder);
  }

  @Override
  public Configurer configureCommandBus(
      @NotNull Function<Configuration, CommandBus> commandBusBuilder) {
    return this.springConfigurer.configureCommandBus(commandBusBuilder);
  }

  @Override
  public Configurer configureQueryBus(@NotNull Function<Configuration, QueryBus> queryBusBuilder) {
    return this.springConfigurer.configureQueryBus(queryBusBuilder);
  }

  @Override
  public Configurer configureQueryUpdateEmitter(
      @NotNull Function<Configuration, QueryUpdateEmitter> queryUpdateEmitterBuilder) {
    return this.springConfigurer.configureQueryUpdateEmitter(queryUpdateEmitterBuilder);
  }

  @Override
  public Configurer configureSerializer(
      @NotNull Function<Configuration, Serializer> serializerBuilder) {
    return this.springConfigurer.configureSerializer(serializerBuilder);
  }

  @Override
  public Configurer configureEventSerializer(
      @NotNull Function<Configuration, Serializer> function) {
    return this.springConfigurer.configureEventSerializer(function);
  }

  @Override
  public Configurer configureMessageSerializer(
      @NotNull Function<Configuration, Serializer> function) {
    return this.springConfigurer.configureMessageSerializer(function);
  }

  @Override
  public Configurer configureTransactionManager(
      @NotNull Function<Configuration, TransactionManager> transactionManagerBuilder) {
    return this.springConfigurer.configureTransactionManager(transactionManagerBuilder);
  }

  @Override
  public Configurer configureResourceInjector(
      @NotNull Function<Configuration, ResourceInjector> resourceInjectorBuilder) {
    return this.springConfigurer.configureResourceInjector(resourceInjectorBuilder);
  }

  @Override
  public Configurer configureTags(@NotNull Function<Configuration, TagsConfiguration> tagsBuilder) {
    return this.springConfigurer.configureTags(tagsBuilder);
  }

  @Override
  public <A> Configurer configureAggregate(
      @NotNull AggregateConfiguration<A> aggregateConfiguration
  ) {
    var aggregateClass = aggregateConfiguration.aggregateType();
    if (aggregateClass.equals(DynamicAggregate.class)) {
      var dynamicConfig = (DynamicAggregateConfiguration) aggregateConfiguration;
      this.configuredAggregates.add(dynamicConfig.getAggregateType());
    } else {
      this.configuredAggregates.add(aggregateClass.getCanonicalName());
    }

    return this.springConfigurer.configureAggregate(aggregateConfiguration);
  }

  @Override
  public <A> Configurer configureAggregate(@NotNull Class<A> aggregate) {
    return this.springConfigurer.configureAggregate(aggregate);
  }

  @Override
  public Configurer registerHandlerDefinition(
      @NotNull BiFunction<Configuration, Class, HandlerDefinition> biFunction) {
    return this.springConfigurer.registerHandlerDefinition(biFunction);
  }

  @Override
  public Configurer registerHandlerEnhancerDefinition(
      Function<Configuration, HandlerEnhancerDefinition> handlerEnhancerBuilder) {
    return this.springConfigurer.registerHandlerEnhancerDefinition(handlerEnhancerBuilder);
  }

  @Override
  public Configurer configureSnapshotter(
      @NotNull Function<Configuration, Snapshotter> snapshotterBuilder) {
    return this.springConfigurer.configureSnapshotter(snapshotterBuilder);
  }

  @Override
  public EventProcessingConfigurer eventProcessing() throws AxonConfigurationException {
    return this.springConfigurer.eventProcessing();
  }

  @Override
  public Configurer eventProcessing(
      @NotNull Consumer<EventProcessingConfigurer> eventProcessingConfigurer)
      throws AxonConfigurationException {
    return this.springConfigurer.eventProcessing(eventProcessingConfigurer);
  }

  @Override
  public Configurer registerEventHandler(
      @NotNull Function<Configuration, Object> eventHandlerBuilder) {
    return this.springConfigurer.registerEventHandler(eventHandlerBuilder);
  }

  @Override
  public void onInitialize(@NotNull Consumer<Configuration> initHandler) {
    this.springConfigurer.onInitialize(initHandler);
  }

  @Override
  public Configuration buildConfiguration() {
    var aggregateConfigurations = this.dynamicAggregateConfigurationsProvider.provide();
    aggregateConfigurations.forEach(this::configureAggregate);
    return this.springConfigurer.buildConfiguration();
  }

  @Override
  public Configuration start() {
    return this.springConfigurer.start();
  }

  @Override
  public void onStart(int phase, LifecycleHandler startHandler) {
    this.springConfigurer.onStart(phase, startHandler);
  }

  @Override
  public void onShutdown(int phase, LifecycleHandler shutdownHandler) {
    this.springConfigurer.onShutdown(phase, shutdownHandler);
  }

  public void configureNewAggregates() {
    var currentConfigs = this.dynamicAggregateConfigurationsProvider.provide();
    currentConfigs.stream()
        .filter(config -> !this.configuredAggregates.contains(config.getAggregateType()))
        .forEach(this.runtimeDynamicAggregateConfigurer::add);
  }
}
