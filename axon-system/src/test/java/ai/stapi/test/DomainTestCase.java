package ai.stapi.test;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;

@Import(DomainTestConfig.class)
@Tag("domain")
public abstract class DomainTestCase extends AbstractDomainTestCase {

}
