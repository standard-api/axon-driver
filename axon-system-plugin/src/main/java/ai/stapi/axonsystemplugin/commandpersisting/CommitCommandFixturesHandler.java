package ai.stapi.axonsystemplugin.commandpersisting;

import ai.stapi.axonsystem.commandpersisting.CommandFixturesCommitted;
import ai.stapi.axonsystem.commandpersisting.CommandMessageStore;
import ai.stapi.axonsystem.commandpersisting.CommitCommandFixtures;
import ai.stapi.axonsystem.commandpersisting.PersistedCommandMessage;
import ai.stapi.axonsystem.configuration.implementations.CommandDispatchedAtInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventhandling.gateway.EventGateway;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitCommandFixturesHandler {

  private final SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyMMddHHmmss");
  private final CommandMessageStore commandMessageStore;
  private final EventGateway eventGateway;
  private final ObjectMapper objectMapper;
  private final Logger logger;

  
  public CommitCommandFixturesHandler(
      CommandMessageStore commandMessageStore,
      EventGateway eventGateway,
      ObjectMapper objectMapper
  ) {
    this.commandMessageStore = commandMessageStore;
    this.eventGateway = eventGateway;
    this.objectMapper = objectMapper;
    this.logger = LoggerFactory.getLogger(CommitCommandFixturesHandler.class);
  }

  @CommandHandler
  public void handle(CommitCommandFixtures command) throws IOException {
    var messages = this.commandMessageStore.getAll();
    var messagesByName = new HashMap<String, List<PersistedCommandMessage<?>>>();
    messages.forEach(message -> {
      var commandName = message.getCommandName();
      if (!messagesByName.containsKey(commandName)) {
        messagesByName.put(commandName, new ArrayList<>());
      }
      messagesByName.get(commandName).add(message);
    });

    for (var entry : messagesByName.entrySet()) {
      var commandName = entry.getKey();
      var directoryPath = command.getOutputDirectoryPath() + File.separator + commandName;
      var directory = new File(directoryPath);
      if (!directory.exists()) {
        directory.mkdirs();
      }
      var messagesOfName = entry.getValue();
      for (var message : messagesOfName) {
        var commandId = message.getTargetAggregateIdentifier();
        var commandMetaData = message.getCommandMetaData();
        var date = this.getDispatchedAtDate(commandName, commandId, commandMetaData);
        if (date.isEmpty()) {
          continue;
        }
        var fileName = this.createFileName(commandId, date.get());
        var filePath = directoryPath + File.separator + fileName;

        // Check if the file already exists and add a number to the filename if it does
        int count = 1;
        while (new File(filePath).exists()) {
          fileName = this.createFileName(commandId + "_" + count, date.get());
          filePath = directoryPath + File.separator + fileName;
          count++;
        }

        try (var writer = new FileWriter(filePath)) {
          writer.write(this.objectMapper.writeValueAsString(message.getCommandPayload()));
        }
      }
    }

    this.eventGateway.publish(new CommandFixturesCommitted());
  }

  @NotNull
  private String createFileName(String commandId, Date date) {
    var dateString = this.fileDateFormat.format(date);
    return dateString + "." + commandId + ".profile.custom.json";
  }

  private Optional<Date> getDispatchedAtDate(
      String commandName,
      String commandId,
      Map<String, Object> commandMetaData
  ) {
    var dispatchedAtMetadataKey = CommandDispatchedAtInterceptor.DISPATCHED_AT_METADATA_KEY;
    var dispatchedAt = commandMetaData.get(dispatchedAtMetadataKey);
    if (dispatchedAt instanceof String dispatchedAtString) {
      try {
        var timestamp = Timestamp.valueOf(dispatchedAtString);
        return Optional.of(new Date(timestamp.getTime()));
      } catch (IllegalArgumentException e) {
        this.logWarning(
            commandName,
            commandId,
            "it could parse metaData value at key: " + dispatchedAtMetadataKey,
            e
        );
      }
    }
    this.logWarning(
        commandName,
        commandId,
        "it does not contain " + dispatchedAtMetadataKey + " field in metaData."
    );
    return Optional.empty();
  }

  private void logWarning(
      String commandName,
      String commandId,
      String becauseMessage
  ) {
    var message = this.formatLogMessage(commandName, commandId, becauseMessage);
    this.logger.warn(message);
  }

  private void logWarning(
      String commandName,
      String commandId,
      String becauseMessage,
      Exception cause
  ) {
    var message = this.formatLogMessage(commandName, commandId, becauseMessage);
    this.logger.warn(message, cause);
  }

  private String formatLogMessage(String commandName, String commandId, String becauseMessage) {
    return String.format(
        "Persisted Command Message of type: '%s' with id: '%s' could not committed, because %s",
        commandName,
        commandId,
        becauseMessage
    );
  }
}
