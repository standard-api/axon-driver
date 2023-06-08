package ai.stapi.axonsystemplugin.commandpersisting;

import ai.stapi.axonsystem.commandpersisting.CommandFixturesCommitted;
import ai.stapi.axonsystem.commandpersisting.CommandMessageStore;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Service;

@Service
public class WipePersistedCommandAfterCommitPolicy {

  private final CommandMessageStore commandMessageStore;

  public WipePersistedCommandAfterCommitPolicy(CommandMessageStore commandMessageStore) {
    this.commandMessageStore = commandMessageStore;
  }

  @EventHandler
  public void on(CommandFixturesCommitted event) {
    this.commandMessageStore.wipeAll();
  }
}
