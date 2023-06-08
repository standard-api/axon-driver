package ai.stapi.axonsystem.configuration;

import ai.stapi.axonsystem.commandpersisting.CommandMessageStore;
import ai.stapi.axonsystem.commandpersisting.InMemoryCommandMessageStore;
import ai.stapi.axonsystem.commandpersisting.PersistCommandInterceptor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@AutoConfiguration
public class CommandPersistingConfiguration {
  
  @Bean
  @ConditionalOnMissingBean(CommandMessageStore.class)
  public InMemoryCommandMessageStore inMemoryCommandMessageStore() {
    return new InMemoryCommandMessageStore();
  }
  
  @Bean
  @ConditionalOnBean(CommandMessageStore.class)
  @Profile("dev")
  public PersistCommandInterceptor persistCommandInterceptor(
      CommandMessageStore commandMessageStore
  ) {
    return new PersistCommandInterceptor(commandMessageStore);
  }
}
