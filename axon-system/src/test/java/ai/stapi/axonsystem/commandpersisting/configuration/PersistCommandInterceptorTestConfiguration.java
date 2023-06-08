package ai.stapi.axonsystem.commandpersisting.configuration;

import ai.stapi.axonsystem.commandpersisting.CommandMessageStore;
import ai.stapi.axonsystem.commandpersisting.PersistCommandInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;


public class PersistCommandInterceptorTestConfiguration {

  @Bean
  public PersistCommandInterceptor persistCommandInterceptor(
      @Autowired CommandMessageStore commandMessageStore
  ) {
    return new PersistCommandInterceptor(commandMessageStore);
  }
}
