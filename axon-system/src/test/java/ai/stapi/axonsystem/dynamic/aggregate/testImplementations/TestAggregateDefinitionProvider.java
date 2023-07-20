package ai.stapi.axonsystem.dynamic.aggregate.testImplementations;

import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionDTO;
import ai.stapi.graphsystem.aggregatedefinition.model.AggregateDefinitionProvider;
import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO;
import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO.CreationPolicy;
import ai.stapi.graphsystem.aggregatedefinition.model.CommandHandlerDefinitionDTO.EventFactory.EventFactoryModification;
import ai.stapi.graphsystem.eventdefinition.EventMessageDefinitionData;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import java.util.List;
import java.util.Map;

public class TestAggregateDefinitionProvider implements AggregateDefinitionProvider {

  public static final String CREATE_VALUE_SET_COMMAND = "CreateValueSet";
  public static final String CHANGE_VALUE_SET_COMMAND = "ChangeValueSet";
  public static final String CREATE_IF_MISSING_VALUE_SET_COMMAND = "CreateIfMissingValueSet";
  public static final String VALUE_SET_CHANGED_EVENT = "ValueSetChanged";
  public static final String VALUE_SET_CREATED_EVENT = "ValueSetCreated";
  public static final String TARGET_RESOURCE = "ValueSet";

  public static final Map<String, AggregateDefinitionDTO> AGGREGATE_DEFINITION_MAP = Map.of(
      TARGET_RESOURCE, new AggregateDefinitionDTO(
          TARGET_RESOURCE,
          "Value Set",
          "Value Set Aggregate Description",
          List.of(
              new CommandHandlerDefinitionDTO(
                  TestOperationDefinitionProvider.provideStatic(CREATE_VALUE_SET_COMMAND),
                  CommandHandlerDefinitionDTO.CreationPolicy.ALWAYS,
                  List.of(
                      new CommandHandlerDefinitionDTO.EventFactory(
                          new EventMessageDefinitionData(
                              VALUE_SET_CREATED_EVENT,
                              "Value Set Created",
                              new StructureDefinitionId("ValueSetCreated"),
                              "Event that a Value Set has been created"
                          ),
                          List.of(
                              EventFactoryModification.add(
                                  "name",
                                  null,
                                  "name"
                              )
                          )
                      )
                  )
              ),
              new CommandHandlerDefinitionDTO(
                  TestOperationDefinitionProvider.provideStatic(CHANGE_VALUE_SET_COMMAND),
                  CommandHandlerDefinitionDTO.CreationPolicy.NEVER,
                  List.of(
                      new CommandHandlerDefinitionDTO.EventFactory(
                          new EventMessageDefinitionData(
                              VALUE_SET_CHANGED_EVENT,
                              "Value Set Changed",
                              new StructureDefinitionId("ValueSetChanged"),
                              "Event that a Value Set has been changed"
                          ),
                          List.of(
                              EventFactoryModification.upsert(
                                  "name",
                                  null,
                                  "name"
                              )
                          )
                      )
                  )
              ),
              new CommandHandlerDefinitionDTO(
                  TestOperationDefinitionProvider.provideStatic(CREATE_IF_MISSING_VALUE_SET_COMMAND),
                  CreationPolicy.IF_MISSING,
                  List.of(
                      new CommandHandlerDefinitionDTO.EventFactory(
                          new EventMessageDefinitionData(
                              VALUE_SET_CHANGED_EVENT,
                              "Value Set Changed",
                              new StructureDefinitionId("ValueSetChanged"),
                              "Event that a Value Set has been changed"
                          ),
                          List.of(
                              EventFactoryModification.add(
                                  "name",
                                  null,
                                  "name"
                              )
                          )
                      )
                  )
              )
          ),
          new StructureDefinitionId(TARGET_RESOURCE)
      )
  );


  @Override
  public List<AggregateDefinitionDTO> provideAll() {
    return TestAggregateDefinitionProvider.AGGREGATE_DEFINITION_MAP.values().stream().toList();
  }

  @Override
  public AggregateDefinitionDTO provide(String aggregateType) {
    return TestAggregateDefinitionProvider.AGGREGATE_DEFINITION_MAP.get(aggregateType);
  }

  @Override
  public AggregateDefinitionDTO getAggregateForOperation(String operationDefinitionId) {
    return TestAggregateDefinitionProvider.AGGREGATE_DEFINITION_MAP.get(TARGET_RESOURCE);
  }

  @Override
  public boolean containsAggregateForOperation(String operationDefinitionId) {
    return false;
  }
}
