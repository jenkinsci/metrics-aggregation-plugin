package io.jenkins.plugins.metrics.analysis;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import hudson.util.StreamTaskListener;

import io.jenkins.plugins.metrics.model.MetricsMeasurement;

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

        List<MetricsMeasurement> measurements = new MetricsActor("Test.java", StreamTaskListener.fromStdout())
                .invoke(workspace, null);

        assertThat(measurements.size()).isEqualTo(9);
        //double cfo = measurements.get(0).getMetrics().getOrDefault("CLASS_FAN_OUT", -1.0);
        double cfo = 0.0;
        assertThat(cfo).isEqualTo(6);
    }
}