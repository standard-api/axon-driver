package ai.stapi.axonsystemplugin.structuredefinition;

import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.ImportStructureDefinition;
import ai.stapi.graphsystem.structuredefinition.command.importStructureDefinitionFromSource.StructureDefinitionImported;
import ai.stapi.schema.structuredefinition.StructureDefinitionId;
import ai.stapi.test.domain.DomainTestCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class StructureDefinitionTest extends DomainTestCase {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void itCanImportStructureDefinitionFromSource() throws IOException {
    var fixtureFilePath = this.getFixtureFilePath("element.profile.canonical.json");
    var file = new File(fixtureFilePath);
    var fileContent = FileUtils.readFileToString(file, "UTF-8");
    Map<String, Object> structureDefinitionSource;
    try {
      structureDefinitionSource = this.objectMapper.readValue(
          fileContent,
          new TypeReference<>() {
          }
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
    var command = new ImportStructureDefinition(
        new StructureDefinitionId("Element"),
        structureDefinitionSource
    );
    this.whenCommandIsDispatched(command);
    this.thenLastEventOfTypeGraphApproved(StructureDefinitionImported.class);
  }
}