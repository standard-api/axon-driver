package ai.stapi.axonsystem.configuration;

import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.MessageOriginProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageOriginCorrelationProvider {

  @Bean
  public static CorrelationDataProvider messageCorrelationProvider() {
    return new MessageOriginProvider();
  }
}
