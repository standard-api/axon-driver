package ai.stapi.formapi.forminfo;

import ai.stapi.test.systemschema.SystemSchemaIntegrationTestCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class FormInfoMapperTest extends SystemSchemaIntegrationTestCase {

    @Autowired
    private FormInfoMapper formInfoMapper;

    @Test
    void itCanMapInfoForCreateCommand() {
        var actual = this.formInfoMapper.map("CreateStructureDefinition");
        this.thenObjectApproved(actual);
    }
    
    @Test
    void itCanMapInfoForUpdateCommand() {
        var actual = this.formInfoMapper.map("UpdateStructureDefinition");
        this.thenObjectApproved(actual);
    }

    @Test
    void itCanMapInfoForAddCommandWithoutModificationWithStartId() {
        var actual = this.formInfoMapper.map("AddElementOnStructureDefinitionDifferential");
        this.thenObjectApproved(actual);
    }

    @Test
    void itCanMapInfoForAddCommandWithModificationWithStartId() {
        var actual = this.formInfoMapper.map("AddTypeOnStructureDefinitionDifferentialElement");
        this.thenObjectApproved(actual);
    }
}