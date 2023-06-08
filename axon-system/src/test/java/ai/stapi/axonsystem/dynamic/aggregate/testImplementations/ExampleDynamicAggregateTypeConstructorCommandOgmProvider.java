package ai.stapi.axonsystem.dynamic.aggregate.testImplementations;

import ai.stapi.graphsystem.messaging.event.AggregateGraphUpdatedEvent;
import ai.stapi.graphoperations.graphLanguage.graphDescription.graphDescriptionBuilder.GraphDescriptionBuilder;
import ai.stapi.graphoperations.graphLanguage.graphDescription.specific.positive.UuidIdentityDescription;
import ai.stapi.identity.UniqueIdentifier;
import ai.stapi.graphsystem.messaging.command.AbstractCommand;
import ai.stapi.graphoperations.objectGraphLanguage.ObjectGraphMapping;
import ai.stapi.graphoperations.objectGraphLanguage.objectGraphMappingBuilder.specific.ogm.ObjectGraphMappingBuilder;
import ai.stapi.graphoperations.objectLanguage.EntityIdentifier;
import ai.stapi.graphsystem.commandEventGraphMappingProvider.specific.SpecificCommandEventGraphMappingProvider;
import java.util.HashMap;
import java.util.Map;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("test")
public class ExampleDynamicAggregateTypeConstructorCommandOgmProvider
    implements SpecificCommandEventGraphMappingProvider {

  public static final String COMMAND_NAME = "ExampleDynamicAggregateTypeConstructorCommand";

  @Override
  public Map<Class<? extends AggregateGraphUpdatedEvent<? extends UniqueIdentifier>>, ObjectGraphMapping> provideGraphMapping(
      AbstractCommand<? extends UniqueIdentifier> command
  ) {
    var map =
        new HashMap<Class<? extends AggregateGraphUpdatedEvent<? extends UniqueIdentifier>>, ObjectGraphMapping>();
    this.addOgmToMap(map);
    return map;
  }

  public void addOgmToMap(
      Map<Class<? extends AggregateGraphUpdatedEvent<? extends UniqueIdentifier>>, ObjectGraphMapping> map
  ) {
    var definition = new ObjectGraphMappingBuilder();
    definition
        .addField("targetIdentifier")
        .setRelation(new EntityIdentifier())
        .addObjectAsObjectFieldMapping()
        .setGraphDescription(new GraphDescriptionBuilder().addNodeDescription(
            TestDynamicAggregateConfigurationProvider.AGGREGATE_TYPE))
        .addField("id")
        .setRelation(new EntityIdentifier())
        .addLeafAsObjectFieldMapping()
        .setGraphDescription(new UuidIdentityDescription());
    definition
        .addField("data")
        .addObjectAsObjectFieldMapping()
        .addField("exampleConstructorAttribute")
        .addLeafAsObjectFieldMapping()
        .setGraphDescription(
            new GraphDescriptionBuilder()
                .addLeafAttribute("exampleConstructorAttribute")
                .addStringAttributeValue());

    map.put(ExampleDynamicAggregateCreatedFactory.ExampleDynamicAggregateCreated.class,
        definition.build());
  }

  @Override
  public boolean supports(String serializationType) {
    return serializationType.equals(COMMAND_NAME);
  }
}
