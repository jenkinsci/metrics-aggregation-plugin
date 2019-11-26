package io.jenkins.plugins.metrics.analysis;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.metrics.model.MetricsReport;
import io.jenkins.plugins.metrics.model.MetricsReportAssert;

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
        measurements.getInfoMessages().forEach(System.out::println);

        MetricsReportAssert.assertThat(measurements).hasNoErrorMessages();
        MetricsReportAssert.assertThat(measurements)
                .hasInfoMessages("Analyzing 1 files matching the pattern Test.java in " + workspace);

        assertThat(measurements.size()).isEqualTo(9);
        double cfo = measurements.get(0).getMetrics().getOrDefault("CLASS_FAN_OUT", -1.0);
        assertThat(cfo).isEqualTo(6);
    }
}