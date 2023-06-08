package ai.stapi.axonsystem.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class SerializerConfiguration {

  @Bean
  @Qualifier("messageSerializer")
  @ConditionalOnBean(ObjectMapper.class)
  public Serializer messageSerializer(
      ObjectMapper objectMapper
  ) {
    return JacksonSerializer
        .builder()
        .objectMapper(objectMapper)
        .build();
  }
}
