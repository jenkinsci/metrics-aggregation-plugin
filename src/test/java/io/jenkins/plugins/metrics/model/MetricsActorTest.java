package io.jenkins.plugins.metrics.model;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.*;

/**
 * Tests the class {@link MetricsActor}.
 *
 * @author Andreas Pabst
 */
class MetricsActorTest {

    private MetricsActor createScanner(final String includePattern) {
        return new MetricsActor(includePattern);
    }

    @Test
    void shouldParseInnerClasses() throws URISyntaxException {
        Path path = Paths.get(MetricsActorTest.class.getResource("Test.java").toURI());
        MetricsReport measurements = createScanner("Test.java")
                .invoke(path.getParent().toFile(), null);

        assertThat(measurements.size()).isEqualTo(9);
        double cfo = measurements.get(0).getMetrics().get("CLASS_FAN_OUT");
        assertThat(cfo).isGreaterThan(1);

        List<Double> cfos = measurements.stream()
                .map(MetricsMeasurement::getMetrics)
                .map(m -> m.get("CLASS_FAN_OUT"))
                .collect(Collectors.toList());

        System.out.println(cfos);
    }
}