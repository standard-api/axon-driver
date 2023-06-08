package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import java.util.ArrayList;
import java.util.List;
import org.axonframework.common.Registration;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.lock.PessimisticLockFactory;
import org.axonframework.config.AggregateConfiguration;
import org.axonframework.config.Configuration;
import org.axonframework.eventsourcing.AggregateFactory;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.snapshotting.SnapshotFilter;
import org.axonframework.lifecycle.Phase;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.modelling.command.Repository;
import org.axonframework.modelling.command.RepositoryProvider;
import org.axonframework.tracing.SpanFactory;

public class DynamicAggregateConfiguration implements AggregateConfiguration<DynamicAggregate> {

  private final DynamicCommandProcessor dynamicCommandProcessor;
  private final EventStore eventStore;
  private final SnapshotTriggerDefinition snapshotTriggerDefinition;
  private final SnapshotFilter snapshotFilter;
  private final RepositoryProvider repositoryProvider;
  private final SpanFactory spanFactory;
  private final Cache cache;
  private final ParameterResolverFactory parameterResolverFactory;
  private final DynamicAggregateModel dynamicAggregateModel;
  private final DynamicAggregateFactory dynamicAggregateFactory;
  private final List<Registration> registrations = new ArrayList<>();
  private final Repository<DynamicAggregate> repository;
  private final DynamicAggregateCommandHandler dynamicAggregateCommandHandler;
  private boolean addedAtRuntime = false;
  private volatile boolean isSubscribed = false;

  public DynamicAggregateConfiguration(
      DynamicCommandProcessor dynamicCommandProcessor,
      EventStore eventStore,
      SnapshotTriggerDefinition snapshotTriggerDefinition,
      SnapshotFilter snapshotFilter,
      RepositoryProvider repositoryProvider,
      SpanFactory spanFactory,
      Cache cache,
      ParameterResolverFactory parameterResolverFactory,
      CommandTargetResolver commandTargetResolver,
      DynamicAggregateModel dynamicAggregateModel
  ) {
    this.dynamicCommandProcessor = dynamicCommandProcessor;
    this.eventStore = eventStore;
    this.snapshotTriggerDefinition = snapshotTriggerDefinition;
    this.snapshotFilter = snapshotFilter;
    this.repositoryProvider = repositoryProvider;
    this.spanFactory = spanFactory;
    this.cache = cache;
    this.parameterResolverFactory = parameterResolverFactory;
    this.dynamicAggregateModel = dynamicAggregateModel;
    this.dynamicAggregateFactory = new DynamicAggregateFactory(this.dynamicCommandProcessor, dynamicAggregateModel);
    this.repository = EventSourcingRepository.builder(DynamicAggregate.class)
        .aggregateModel(this.dynamicAggregateModel)
        .lockFactory(PessimisticLockFactory.usingDefaults())
        .eventStore(this.eventStore)
        .snapshotTriggerDefinition(snapshotTriggerDefinition)
        .aggregateFactory(this.dynamicAggregateFactory)
        .repositoryProvider(this.repositoryProvider)
        .spanFactory(this.spanFactory)
        .cache(this.cache)
        .build();

    this.dynamicAggregateCommandHandler = new DynamicAggregateCommandHandler.Builder()
        .aggregateModel(dynamicAggregateModel)
        .repository(this.repository)
        .parameterResolverFactory(this.parameterResolverFactory)
        .dynamicCommandProcessor(this.dynamicCommandProcessor)
        .commandTargetResolver(commandTargetResolver)
        .build();
  }

  @Override
  public Repository<DynamicAggregate> repository() {
    return this.repository;
  }

  @Override
  public Class<DynamicAggregate> aggregateType() {
    return DynamicAggregate.class;
  }

  @Override
  public AggregateFactory<DynamicAggregate> aggregateFactory() {
    return this.dynamicAggregateFactory;
  }

  @Override
  public SnapshotFilter snapshotFilter() {
    return this.snapshotFilter;
  }

  @Override
  public void initialize(Configuration config) {
    if (this.addedAtRuntime && !this.isSubscribed) {
      this.registrations.add(
          dynamicAggregateCommandHandler.subscribe(config.commandBus())
      );
      this.isSubscribed = true;
    } else {
      config.onStart(
          Phase.LOCAL_MESSAGE_HANDLER_REGISTRATIONS,
          () -> {
            //TODO: We need to find out why the callback gets registered twice to the configuration (DefaultConfigurer:867)
            if (!this.isSubscribed) {
              this.isSubscribed = true;
              registrations.add(
                  dynamicAggregateCommandHandler.subscribe(config.commandBus())
              );
            }
          }
      );
    }
    config.onShutdown(
        Phase.LOCAL_MESSAGE_HANDLER_REGISTRATIONS,
        () -> {
          registrations.forEach(Registration::cancel);
          registrations.clear();
        }
    );
  }

  public void setAsAddedAtRuntime() {
    this.addedAtRuntime = true;
  }

  public String getAggregateType() {
    return this.dynamicAggregateModel.type();
  }
}
