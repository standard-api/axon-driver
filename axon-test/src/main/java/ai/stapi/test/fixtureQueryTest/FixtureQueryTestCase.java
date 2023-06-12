package ai.stapi.test.fixtureQueryTest;

import org.junit.jupiter.api.Tag;
import org.springframework.context.annotation.Import;

@Tag("fixture_query")
@Import(FixtureQueryTestCaseConfig.class)
public abstract class FixtureQueryTestCase extends AbstractFixtureQueryTestCase {
}
