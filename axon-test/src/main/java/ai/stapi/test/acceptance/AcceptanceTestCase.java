package ai.stapi.test.acceptance;

import ai.stapi.test.base.AbstractAxonTestCase;
import org.junit.jupiter.api.Tag;
import org.springframework.test.context.ContextConfiguration;

@Tag("acceptance")
@ContextConfiguration(classes = {AcceptanceTestCaseConfig.class})
public abstract class AcceptanceTestCase extends AbstractAxonTestCase {

}
