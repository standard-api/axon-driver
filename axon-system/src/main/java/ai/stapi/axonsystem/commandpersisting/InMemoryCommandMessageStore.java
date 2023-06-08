package ai.stapi.axonsystem.commandpersisting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.axonframework.commandhandling.CommandMessage;

public class InMemoryCommandMessageStore implements CommandMessageStore {

  private final List<PersistedCommandMessage<?>> persistedCommandMessages;

  public InMemoryCommandMessageStore() {
    this.persistedCommandMessages = new ArrayList<>();
  }

  @Override
  public void storeCommand(CommandMessage<?> commandMessage) {
    var id = this.extractId(commandMessage);
    var payload = this.extractPayload(commandMessage);
    var persisted = new InMemoryPersistedCommandMessage<>(
        commandMessage.getIdentifier(),
        commandMessage.getCommandName(),
        payload,
        commandMessage.getMetaData(),
        id
    );
    this.persistedCommandMessages.add(persisted);
  }

  @Override
  public List<PersistedCommandMessage<?>> getAll() {
    return this.persistedCommandMessages;
  }

  @Override
  public void wipeAll() {
    this.persistedCommandMessages.clear();
  }

  private static class InMemoryPersistedCommandMessage<T> implements PersistedCommandMessage<T> {

    @JsonIgnore
    private final String key;
    private final String commandName;
    private final T commandPayload;
    private final Map<String, Object> commandMetaData;
    private final String targetAggregateIdentifier;

    public InMemoryPersistedCommandMessage(
        String key,
        String commandName,
        T commandPayload,
        Map<String, Object> commandMetaData,
        String targetAggregateIdentifier
    ) {
      this.key = key;
      this.commandName = commandName;
      this.commandPayload = commandPayload;
      this.commandMetaData = commandMetaData;
      this.targetAggregateIdentifier = targetAggregateIdentifier;
    }

    @Override
    @JsonIgnore
    public String getKey() {
      return this.key;
    }

    @Override
    public String getCommandName() {
      return this.commandName;
    }

    @Override
    public T getCommandPayload() {
      return this.commandPayload;
    }

    @Override
    public Map<String, Object> getCommandMetaData() {
      return this.commandMetaData;
    }

    @Override
    public String getTargetAggregateIdentifier() {
      return this.targetAggregateIdentifier;
    }
  }
}
