package io.jenkins.plugins.metrics.extension;

import org.junit.Test;

import edu.hm.hafner.coverage.ClassNode;
import edu.hm.hafner.coverage.Coverage.CoverageBuilder;
import edu.hm.hafner.coverage.Metric;
import edu.hm.hafner.coverage.Node;

import java.util.List;

import hudson.model.Run;

import io.jenkins.plugins.coverage.metrics.steps.CoverageBuildAction;

import static io.jenkins.plugins.metrics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Java doc comment.
 */
public class CoverageMetricsProviderFactoryTest {
    /**
     * Java doc comment.
     */
    @Test
    public void shouldGetMetricProviders() {
        var coverageMetricsProviderFactory = new CoverageMetricsProviderFactory();
        var coverageResult = mock(Node.class);
        var classNode = new ClassNode("com.example.MyClass");
        var lineCoverage = new CoverageBuilder().withMetric(Metric.LINE).withCovered(96).withMissed(4).build();
        classNode.addValue(lineCoverage);

        when(coverageResult.getAllClassNodes()).thenReturn(List.of(classNode));

        var action = mock(CoverageBuildAction.class);
        when(action.getResult()).thenReturn(coverageResult);

        var run = mock(Run.class);
        when(run.getActions(CoverageBuildAction.class)).thenReturn(List.of(action));

        var metricsProvider = coverageMetricsProviderFactory.getMetricsProviderFor(run);
        var measurements = metricsProvider.getMetricsMeasurements();
        var measurement = measurements.get(0);
        var value = measurement.getMetric("LINE_COVERAGE");
        assertThat(value.get().doubleValue()).isEqualTo(96.0);
    }
}
