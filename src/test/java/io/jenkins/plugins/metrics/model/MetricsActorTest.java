package io.jenkins.plugins.metrics.model;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.*;

/**
 * Tests the class {@link MetricsActor}.
 *
 * @author Andreas Pabst
 */
class MetricsActorTest {

    @Test
    void shouldParseInnerClasses() throws URISyntaxException {
        File workspace = Paths.get(MetricsActorTest.class.getResource("Test.java").toURI()).getParent().toFile();

        MetricsReport measurements = new MetricsActor("Test.java")
                .invoke(workspace, null);

        MetricsReportAssert.assertThat(measurements).hasNoErrorMessages();
        MetricsReportAssert.assertThat(measurements)
                .hasInfoMessages("Analyzing 1 files matching the pattern Test.java in " + workspace);

        assertThat(measurements.size()).isEqualTo(9);
        double cfo = measurements.get(0).getMetrics().get("CLASS_FAN_OUT");
        assertThat(cfo).isEqualTo(6);
    }
}