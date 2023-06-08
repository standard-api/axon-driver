/*
 * Copyright (c) 2010-2022. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.stapi.axonsystem.dynamic.aggregate;

import static org.axonframework.common.BuilderUtils.assertNonNull;

import ai.stapi.graphsystem.dynamiccommandprocessor.DynamicCommandProcessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandMessageHandler;
import org.axonframework.commandhandling.NoHandlerForCommandException;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.Registration;
import org.axonframework.messaging.MessageHandler;
import org.axonframework.messaging.annotation.HandlerDefinition;
import org.axonframework.messaging.annotation.MessageHandlingMember;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.Aggregate;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.modelling.command.CommandTargetResolver;
import org.axonframework.modelling.command.Repository;
import org.axonframework.modelling.command.VersionedAggregateIdentifier;
import org.axonframework.modelling.command.inspection.AggregateModel;


public class DynamicAggregateCommandHandler implements CommandMessageHandler {

  private final Repository<DynamicAggregate> repository;
  private final CommandTargetResolver commandTargetResolver;
  private final List<MessageHandler<CommandMessage<?>>> handlers;
  private final Set<String> supportedCommandNames;
  private final Map<String, Set<MessageHandler<CommandMessage<?>>>> supportedCommandsByName;
  private final DynamicCommandProcessor dynamicCommandProcessor;
  private final DynamicAggregateModel dynamicAggregateModel;

  public static Builder builder() {
    return new Builder();
  }

  protected DynamicAggregateCommandHandler(
      Builder builder
  ) {
    builder.validate();
    this.repository = builder.repository;
    this.commandTargetResolver = builder.commandTargetResolver;
    this.supportedCommandNames = new HashSet<>();
    this.supportedCommandsByName = new HashMap<>();
    this.dynamicCommandProcessor = builder.dynamicCommandProcessor;
    this.dynamicAggregateModel = builder.buildAggregateModel();
    this.handlers = initializeHandlers(this.dynamicAggregateModel);
  }

  public Registration subscribe(CommandBus commandBus) {
    var subscriptions = supportedCommandsByName
        .entrySet()
        .stream()
        .flatMap(entry -> entry.getValue().stream().map(
            messageHandler -> commandBus.subscribe(
                entry.getKey(),
                messageHandler
            )
        ))
        .filter(Objects::nonNull)
        .toList();

    return () -> subscriptions.stream().map(Registration::cancel)
        .reduce(Boolean::logicalOr)
        .orElse(false);
  }

  private List<MessageHandler<CommandMessage<?>>> initializeHandlers(
      DynamicAggregateModel aggregateModel
  ) {
    List<MessageHandler<CommandMessage<?>>> handlersFound = new ArrayList<>();

    aggregateModel.allCommandHandlers()
        .values()
        .stream()
        .flatMap(List::stream)
        .forEach(commandHandler -> initializeHandler(
            aggregateModel,
            commandHandler,
            handlersFound
        ));

    return handlersFound;
  }

  private void initializeHandler(
      DynamicAggregateModel dynamicAggregateModel,
      MessageHandlingMember<? super DynamicAggregate> handler,
      List<MessageHandler<CommandMessage<?>>> handlersFound
  ) {
    MessageHandler<CommandMessage<?>> messageHandler = null;
    String commandName = null;
    var createHandler = handler.unwrap(
        DynamicConstructorCommandMessageHandlerMember.class
    );
    if (createHandler.isPresent()) {
      messageHandler = new AggregateConstructorCommandHandler(createHandler.get());
      commandName = createHandler.get().commandName();
    }
    var createOrUpdate = handler.unwrap(
        DynamicCreateIfMissingCommandMessageHandlerMember.class
    );
    if (createOrUpdate.isPresent()) {
      messageHandler = new AggregateCreateOrUpdateCommandHandler(
          createOrUpdate.get(),
          this.dynamicCommandProcessor,
          dynamicAggregateModel.type()
      );
      commandName = createOrUpdate.get().commandName();
    }
    var neverHandler = handler.unwrap(
        DynamicMethodCommandMessageHandlerMember.class
    );
    if (neverHandler.isPresent()) {
      messageHandler = new AggregateCommandHandler(neverHandler.get());
      commandName = neverHandler.get().commandName();
    }
    if (messageHandler == null) {
      throw new UnknownDynamicCommandHandlerException(
          "Unknown dynamic command message handling member in aggregate configuration."
      );
    }
    handlersFound.add(messageHandler);
    supportedCommandsByName
        .computeIfAbsent(commandName, key -> new HashSet<>())
        .add(messageHandler);
    supportedCommandNames.add(commandName);
  }

  @Override
  public Object handle(CommandMessage<?> commandMessage) throws Exception {
    return handlers.stream()
        .filter(ch -> ch.canHandle(commandMessage))
        .findFirst()
        .orElseThrow(() -> new NoHandlerForCommandException(commandMessage))
        .handle(commandMessage);
  }

  @Override
  public boolean canHandle(CommandMessage<?> message) {
    return handlers.stream().anyMatch(ch -> ch.canHandle(message));
  }

  protected Object resolveReturnValue(
      @SuppressWarnings("unused") CommandMessage<?> command,
      Aggregate<DynamicAggregate> createdAggregate
  ) {
    return createdAggregate.identifier();
  }

  @Override
  public Set<String> supportedCommandNames() {
    return supportedCommandNames;
  }

  public static class Builder {

    private Repository<DynamicAggregate> repository;
    private DynamicCommandProcessor dynamicCommandProcessor;
    private CommandTargetResolver commandTargetResolver;
    private ParameterResolverFactory parameterResolverFactory;
    private HandlerDefinition handlerDefinition;
    private DynamicAggregateModel aggregateModel;

    public Builder repository(Repository<DynamicAggregate> repository) {
      assertNonNull(repository, "Repository may not be null");
      this.repository = repository;
      return this;
    }

    public Builder dynamicCommandProcessor(DynamicCommandProcessor dynamicCommandProcessor) {
      assertNonNull(dynamicCommandProcessor, "Repository may not be null");
      this.dynamicCommandProcessor = dynamicCommandProcessor;
      return this;
    }


    public Builder commandTargetResolver(CommandTargetResolver commandTargetResolver) {
      assertNonNull(commandTargetResolver, "CommandTargetResolver may not be null");
      this.commandTargetResolver = commandTargetResolver;
      return this;
    }


    public Builder parameterResolverFactory(ParameterResolverFactory parameterResolverFactory) {
      assertNonNull(parameterResolverFactory, "ParameterResolverFactory may not be null");
      this.parameterResolverFactory = parameterResolverFactory;
      return this;
    }

    public Builder handlerDefinition(HandlerDefinition handlerDefinition) {
      assertNonNull(handlerDefinition, "HandlerDefinition may not be null");
      this.handlerDefinition = handlerDefinition;
      return this;
    }

    public Builder aggregateModel(DynamicAggregateModel aggregateModel) {
      assertNonNull(aggregateModel, "AggregateModel may not be null");
      this.aggregateModel = aggregateModel;
      return this;
    }

    /**
     * Instantiate the {@link AggregateModel} of generic type {@code T} describing the structure of
     * the Aggregate this {@link DynamicAggregateCommandHandler} will handle commands for.
     *
     * @return a {@link AggregateModel} of generic type {@code T} describing the Aggregate this
     * {@link DynamicAggregateCommandHandler} will handle commands for
     */
    private DynamicAggregateModel buildAggregateModel() {
      return aggregateModel;
    }

    public DynamicAggregateCommandHandler build() {
      return new DynamicAggregateCommandHandler(this);
    }

    /**
     * Validates whether the fields contained in this Builder are set accordingly.
     *
     * @throws AxonConfigurationException if one field is asserted to be incorrect according to the
     *                                    Builder's specifications
     */
    protected void validate() throws AxonConfigurationException {
      assertNonNull(repository, "The Repository is a hard requirement and should be provided");
      assertNonNull(
          aggregateModel,
          "No AggregateModel is set, but it is a hard requirement"
      );
      assertNonNull(
          dynamicCommandProcessor,
          "No Dynamic Command Processor is set, but it is a hard requirement"
      );
    }
  }

  private class AggregateConstructorCommandHandler implements MessageHandler<CommandMessage<?>> {

    private final MessageHandlingMember<?> handler;

    public AggregateConstructorCommandHandler(MessageHandlingMember<?> handler) {
      this.handler = handler;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object handle(CommandMessage<?> command) throws Exception {
      var aggregate = repository.newInstance(
          () -> (DynamicAggregate) handler.handle(command, null)
      );
      return resolveReturnValue(command, aggregate);
    }

    @Override
    public boolean canHandle(CommandMessage<?> message) {
      return handler.canHandle(message);
    }
  }

  private class AggregateCreateOrUpdateCommandHandler implements MessageHandler<CommandMessage<?>> {

    private final MessageHandlingMember<? super DynamicAggregate> handler;
    private final DynamicCommandProcessor dynamicCommandProcessor;

    private final String aggregateType;

    public AggregateCreateOrUpdateCommandHandler(
        MessageHandlingMember<? super DynamicAggregate> handler,
        DynamicCommandProcessor dynamicCommandProcessor,
        String aggregateType
    ) {
      this.handler = handler;
      this.dynamicCommandProcessor = dynamicCommandProcessor;
      this.aggregateType = aggregateType;
    }

    @Override
    public Object handle(CommandMessage<?> command) throws Exception {
      VersionedAggregateIdentifier iv = commandTargetResolver.resolveTarget(command);
      return repository.loadOrCreate(
          iv.getIdentifier(),
          () -> new DynamicAggregate(this.dynamicCommandProcessor, this.aggregateType)
      ).handle(command);
    }

    @Override
    public boolean canHandle(CommandMessage<?> message) {
      return handler.canHandle(message);
    }
  }

  private class AggregateCommandHandler implements MessageHandler<CommandMessage<?>> {

    private final MessageHandlingMember<? super DynamicAggregate> handler;

    public AggregateCommandHandler(MessageHandlingMember<? super DynamicAggregate> handler) {
      this.handler = handler;
    }

    @Override
    public Object handle(CommandMessage<?> command) throws Exception {
      var identifier = commandTargetResolver.resolveTarget(command);
      Aggregate<DynamicAggregate> aggregate;
      try {
        aggregate = repository.load(identifier.getIdentifier(), identifier.getVersion());
      } catch (AggregateNotFoundException e) {
        throw new AggregateNotFoundException(
            identifier.getIdentifier(),
            String.format(
                "Cannot handle command [%s] in aggregate [%s] with id [%s].%n%s%s",
                command.getCommandName(),
                dynamicAggregateModel.type(),
                identifier.getIdentifier(),
                "Command handler cannot construct new aggregate instance",
                ", but aggregate with such id was not found."
            ),
            e
        );
      }
      return aggregate.handle(command);
    }

    @Override
    public boolean canHandle(CommandMessage<?> message) {
      return handler.canHandle(message);
    }
  }

  private static class UnknownDynamicCommandHandlerException extends RuntimeException {

    public UnknownDynamicCommandHandlerException(String message) {
      super(message);
    }

    public UnknownDynamicCommandHandlerException(String message, Throwable cause) {
      super(message, cause);
    }
  }
}
