package ai.stapi.axonsystem.commandpersisting;

import java.util.Map;

public interface PersistedCommandMessage<T> {

  String getKey();

  String getCommandName();

  T getCommandPayload();

  Map<String, Object> getCommandMetaData();

  String getTargetAggregateIdentifier();
}
