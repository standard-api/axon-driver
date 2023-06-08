package ai.stapi.axonsystem.dynamic.aggregate.testImplementations;

import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregate;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateConfiguration;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateConfigurationsProvider;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicAggregateModel;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicConstructorCommandMessageHandlerMember;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicCreateIfMissingCommandMessageHandlerMember;
import ai.stapi.axonsystem.dynamic.aggregate.DynamicMethodCommandMessageHandlerMember;
import ai.stapi.graphsystem.messaging.event.AggregateGraphUpdatedEvent;
import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import ai.stapi.graphsystem.messaging.event.GraphUpdatedEvent;
import java.util.List;
import org.axonframework.config.Configuration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventsourcing.NoSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.snapshotting.RevisionSnapshotFilter;
import org.axonframework.messaging.annotation.AnnotatedMessageHandlingMember;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.tracing.SpanFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;

public class TestDynamicAggregateConfigurationProvider implements DynamicAggregateConfigurationsProvider {

  public static final String AGGREGATE_TYPE = "ExampleDynamicAggregateType";
  private final DynamicCommandProcessor dynamicCommandProcessor;
  private final EventStore eventStore;
  private final SpanFactory spanFactory;
  private final Configuration configuration;
  private final ParameterResolverFactory parameterResolverFactory;
  private final CommandTargetResolver commandTargetResolver;

  public TestDynamicAggregateConfigurationProvider(
      DynamicCommandProcessor dynamicCommandProcessor,
      @Lazy EventStore eventStore,
      SpanFactory spanFactory,
      @Lazy Configuration configuration,
      ParameterResolverFactory parameterResolverFactory,
      CommandTargetResolver commandTargetResolver
  ) {
    this.dynamicCommandProcessor = dynamicCommandProcessor;
    this.eventStore = eventStore;
    this.spanFactory = spanFactory;
    this.configuration = configuration;
    this.parameterResolverFactory = parameterResolverFactory;
    this.commandTargetResolver = commandTargetResolver;
  }

  @Override
  public List<DynamicAggregateConfiguration> provide() {
    var aggregateConfiguration = getDynamicAggregateConfiguration();
    return List.of(aggregateConfiguration);
  }

  @Override
  public DynamicAggregateConfiguration provide(String aggregateType) {
    return this.getDynamicAggregateConfiguration();
  }

  @NotNull
  private DynamicAggregateConfiguration getDynamicAggregateConfiguration() {
    var dynamicAggregateModel = new DynamicAggregateModel
        .Builder(TestDynamicAggregateConfigurationProvider.AGGREGATE_TYPE)
        .addCommandHandler(
            new DynamicConstructorCommandMessageHandlerMember(
                this.dynamicCommandProcessor,
                TestDynamicAggregateConfigurationProvider.AGGREGATE_TYPE,
                ExampleDynamicAggregateTypeConstructorCommandOgmProvider.COMMAND_NAME
            )
        )
        .addCommandHandler(
            new DynamicCreateIfMissingCommandMessageHandlerMember(
                this.dynamicCommandProcessor,
                TestDynamicAggregateConfigurationProvider.AGGREGATE_TYPE,
                ExampleDynamicAggregateTypeCreateIfMissingCommandOgmProvider.COMMAND_NAME
            )
        )
        .addCommandHandler(
            new DynamicMethodCommandMessageHandlerMember(
                this.dynamicCommandProcessor,
                TestDynamicAggregateConfigurationProvider.AGGREGATE_TYPE,
                ExampleDynamicAggregateTypeMethodCommandOgmProvider.COMMAND_NAME
            )
        )
        .addEventHandler(this.createOnEventSourcingHandler())
        .build();

    return new DynamicAggregateConfiguration(
        this.dynamicCommandProcessor,
        this.eventStore,
        NoSnapshotTriggerDefinition.INSTANCE,
        RevisionSnapshotFilter.builder()
            .type(TestDynamicAggregateConfigurationProvider.AGGREGATE_TYPE)
            .revision(null)
            .build(),
        this.configuration::repository,
        this.spanFactory,
        null,
        this.parameterResolverFactory,
        this.commandTargetResolver,
        dynamicAggregateModel
    );
  }

  @NotNull
  private AnnotatedMessageHandlingMember<DynamicAggregate> createOnEventSourcingHandler() {
    try {
      return new AnnotatedMessageHandlingMember<>(
          DynamicAggregate.class.getMethod("onEvent", AggregateGraphUpdatedEvent.class),
          EventMessage.class,
          GraphUpdatedEvent.class,
          this.parameterResolverFactory
      );
    } catch (NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
