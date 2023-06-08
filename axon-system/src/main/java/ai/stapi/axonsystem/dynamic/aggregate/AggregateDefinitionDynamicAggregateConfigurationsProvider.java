package ai.stapi.axonsystem.dynamic.aggregate;

import ai.stapi.axonsystem.dynamic.aggregate.exceptions.CannotProvideDynamicAggregateConfiguration;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionDTO;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO.CreationPolicy;
import ai.stapi.graphsystem.aggregatedefinition.model.exceptions.CannotProvideAggregateDefinition;
import ai.stapi.axonsystem.dynamic.aggregate.exceptions.CannotProvideDynamicAggregateConfigurationFromAggregateDefinition;
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
import org.springframework.stereotype.Service;

@Service
public class AggregateDefinitionDynamicAggregateConfigurationsProvider
    implements DynamicAggregateConfigurationsProvider {

  private final AggregateDefinitionProvider aggregateDefinitionProvider;
  private final DynamicCommandProcessor dynamicCommandProcessor;
  private final EventStore eventStore;
  private final SpanFactory spanFactory;
  private final Configuration configuration;
  private final ParameterResolverFactory parameterResolverFactory;
  
  private final CommandTargetResolver commandTargetResolver;

  public AggregateDefinitionDynamicAggregateConfigurationsProvider(
      AggregateDefinitionProvider aggregateDefinitionProvider,
      DynamicCommandProcessor dynamicCommandProcessor,
      @Lazy EventStore eventStore,
      SpanFactory spanFactory,
      @Lazy Configuration configuration,
      ParameterResolverFactory parameterResolverFactory,
      CommandTargetResolver commandTargetResolver
  ) {
    this.aggregateDefinitionProvider = aggregateDefinitionProvider;
    this.dynamicCommandProcessor = dynamicCommandProcessor;
    this.eventStore = eventStore;
    this.spanFactory = spanFactory;
    this.configuration = configuration;
    this.parameterResolverFactory = parameterResolverFactory;
    this.commandTargetResolver = commandTargetResolver;
  }

  @Override
  public List<DynamicAggregateConfiguration> provide() {
    var aggregateDefinitions = this.aggregateDefinitionProvider.provideAll();
    return aggregateDefinitions.stream().map(this::createDynamicAggregateConfiguration).toList();
  }

  @Override
  public DynamicAggregateConfiguration provide(
      String aggregateType
  ) throws CannotProvideDynamicAggregateConfiguration {
    AggregateDefinitionDTO aggregateDefinition;
    try {
      aggregateDefinition = this.aggregateDefinitionProvider.provide(aggregateType);
    } catch (CannotProvideAggregateDefinition e) {
      throw new CannotProvideDynamicAggregateConfiguration(aggregateType, e);
    }
    return this.createDynamicAggregateConfiguration(aggregateDefinition);
  }

  @NotNull
  private DynamicAggregateConfiguration createDynamicAggregateConfiguration(
      AggregateDefinitionDTO definition
  ) {
    var aggregateType = definition.getName();
    var modelBuilder = new DynamicAggregateModel.Builder(aggregateType);
    modelBuilder.addEventHandler(this.createOnEventSourcingHandler());
    definition.getCommand().stream().map(commandDefinition -> {
      var creationalPolicy = commandDefinition.getCreationalPolicy();
      var commandName = commandDefinition.getOperation().getId();
      if (creationalPolicy.equals(CreationPolicy.ALWAYS)) {
        return new DynamicConstructorCommandMessageHandlerMember(
            this.dynamicCommandProcessor,
            aggregateType,
            commandName
        );
      }
      if (creationalPolicy.equals(CreationPolicy.NEVER)) {
        return new DynamicMethodCommandMessageHandlerMember(
            this.dynamicCommandProcessor,
            aggregateType,
            commandName
        );
      }
      if (creationalPolicy.equals(CreationPolicy.IF_MISSING)) {
        return new DynamicCreateIfMissingCommandMessageHandlerMember(
            this.dynamicCommandProcessor,
            aggregateType,
            commandName
        );
      }
      throw CannotProvideDynamicAggregateConfigurationFromAggregateDefinition.becauseCommandHandlerDefinitionHasUnknownCreationalPolicy(
          commandDefinition
      );
    }).forEach(modelBuilder::addCommandHandler);
    return new DynamicAggregateConfiguration(
        this.dynamicCommandProcessor,
        this.eventStore,
        NoSnapshotTriggerDefinition.INSTANCE,
        RevisionSnapshotFilter.builder()
            .type(aggregateType)
            .revision(null)
            .build(),
        this.configuration::repository,
        this.spanFactory,
        null,
        this.parameterResolverFactory,
        this.commandTargetResolver,
        modelBuilder.build()
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
