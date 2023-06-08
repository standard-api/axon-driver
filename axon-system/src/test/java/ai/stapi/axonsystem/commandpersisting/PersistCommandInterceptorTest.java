package ai.stapi.axonsystem.commandpersisting;

import ai.stapi.axonsystem.commandpersisting.configuration.ExampleModuleDefinitionsLoader;
import ai.stapi.axonsystem.commandpersisting.configuration.PersistCommandInterceptorTestConfiguration;
import ai.stapi.axonsystem.commandpersisting.exampleimplementation.ChangeExampleNodeExampleQuantity;
import ai.stapi.axonsystem.commandpersisting.exampleimplementation.CreateNewExampleNodeCommand;
import ai.stapi.identity.UniversallyUniqueIdentifier;
import ai.stapi.objectRenderer.infrastructure.objectToJsonStringRenderer.ObjectToJSonStringOptions.RenderFeature;
import ai.stapi.test.DomainTestCase;
import ai.stapi.test.schemaintegration.StructureDefinitionScope;
import java.util.UUID;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

@Import(PersistCommandInterceptorTestConfiguration.class)
@StructureDefinitionScope(ExampleModuleDefinitionsLoader.SCOPE)
class PersistCommandInterceptorTest extends DomainTestCase {

  @Autowired
  private CommandMessageStore commandMessageStore;

  @Autowired
  private CommandGateway commandGateway;

  @BeforeEach
  void clean() {
    this.commandMessageStore.wipeAll();
  }

  @Test
  void itWillInterceptAndPersistCommands() {
    this.whenCommandIsDispatched(
        new CreateNewExampleNodeCommand(
            new UniversallyUniqueIdentifier(UUID.randomUUID()),
            "ExampleNodeType",
            "Example Name"
        )
    );
    this.whenCommandIsDispatched(
        new CreateNewExampleNodeCommand(
            new UniversallyUniqueIdentifier(UUID.randomUUID()),
            "ExampleNodeType",
            "Example Name 2"
        )
    );
    this.whenCommandIsDispatched(
        new CreateNewExampleNodeCommand(
            new UniversallyUniqueIdentifier(UUID.randomUUID()),
            "ExampleNodeType2",
            "Example Name 3"
        )
    );
    this.thenObjectApproved(
        this.commandMessageStore.getAll(),
        RenderFeature.HIDE_DISPATCHED_AT,
        RenderFeature.HIDE_IDS,
        RenderFeature.SORT_FIELDS,
        RenderFeature.HIDE_KEY_HASHCODE
    );
  }

  @Test
  void itWillNotInterceptFailedCommands() {
    this.commandGateway.send(
        new CreateNewExampleNodeCommand(
            new UniversallyUniqueIdentifier(UUID.randomUUID()),
            "ExampleNodeType",
            "Example Name"
        )
    );
    this.commandGateway.send(
        new ChangeExampleNodeExampleQuantity(
            new UniversallyUniqueIdentifier(UUID.randomUUID()),
            50
        )
    );
    this.thenObjectApproved(
        this.commandMessageStore.getAll(),
        RenderFeature.HIDE_DISPATCHED_AT,
        RenderFeature.HIDE_IDS,
        RenderFeature.SORT_FIELDS,
        RenderFeature.HIDE_KEY_HASHCODE
    );
  }

}