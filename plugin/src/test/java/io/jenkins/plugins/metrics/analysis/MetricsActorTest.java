package io.jenkins.plugins.metrics.analysis;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import hudson.util.StreamTaskListener;

import io.jenkins.plugins.metrics.extension.PMDMetricsProviderFactory;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.DoubleMetric;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;

import static org.assertj.core.api.Assertions.*;

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

        assertThat(measurements).hasSize(5);

        String fileName = MetricsActorTest.class.getResource("Test.java").toURI().getPath();
        ClassMetricsMeasurement testClass = createClassMetricsMeasurement("Test",
                "com.example.anotherpackage", fileName);
        testClass.addMetric(createDoubleMetric("TCC", 0.0));
        testClass.addMetric(createDoubleMetric("WOC", 1.0));
        testClass.addMetric(createDoubleMetric("WMC", 2.0));
        testClass.addMetric(createIntegerMetric("LOC", 20));
        testClass.addMetric(createIntegerMetric("NCSS", 10));
        testClass.addMetric(createIntegerMetric("CLASS_FAN_OUT", 6));
        testClass.addMetric(createIntegerMetric("NOPA", 0));
        testClass.addMetric(createIntegerMetric("NOAM", 0));
        testClass.addMetric(createIntegerMetric("ATFD", 0));
        assertThat(measurements).contains(testClass);

        ClassMetricsMeasurement staticInnerClass = createClassMetricsMeasurement("Test$StaticInnerClass",
                "com.example.anotherpackage", fileName);
        staticInnerClass.addMetric(createDoubleMetric("TCC", Double.NaN));
        staticInnerClass.addMetric(createDoubleMetric("WOC", 1.0));
        staticInnerClass.addMetric(createDoubleMetric("WMC", 1.0));
        staticInnerClass.addMetric(createIntegerMetric("LOC", 5));
        staticInnerClass.addMetric(createIntegerMetric("NCSS", 3));
        staticInnerClass.addMetric(createIntegerMetric("CLASS_FAN_OUT", 2));
        staticInnerClass.addMetric(createIntegerMetric("NOPA", 0));
        staticInnerClass.addMetric(createIntegerMetric("NOAM", 0));
        staticInnerClass.addMetric(createIntegerMetric("ATFD", 0));
        assertThat(measurements).contains(staticInnerClass);

    }

    @Test
    void shouldLogProgress() throws URISyntaxException, IOException {
        File workspace = Paths.get(MetricsActorTest.class.getResource("Test.java").toURI()).getParent().toFile();

        PipedInputStream pipeInput = new PipedInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(pipeInput));
        BufferedOutputStream out = new BufferedOutputStream(new PipedOutputStream(pipeInput));
        StreamTaskListener taskListener = new StreamTaskListener(out);

        List<MetricsMeasurement> measurements = new MetricsActor("Test*.java", taskListener)
                .invoke(workspace, null);

        out.flush();
        out.close();
        List<String> log = reader.lines().collect(Collectors.toList());
        assertThat(log).containsExactly(
                "[Metrics] Analyzing 3 files matching the pattern 'Test*.java' in " + workspace.getPath(),
                "[Metrics] Analyzed 1 files (33%)",
                "[Metrics] Analyzed 2 files (66%)",
                "[Metrics] Analyzed 3 files (100%)");
    }

    private ClassMetricsMeasurement createClassMetricsMeasurement(final String className, final String packageName,
            final String fileName) {
        ClassMetricsMeasurement classMetricsMeasurement = new ClassMetricsMeasurement();
        classMetricsMeasurement.setClassName(className);
        classMetricsMeasurement.setPackageName(packageName);
        classMetricsMeasurement.setFileName(fileName);

        return classMetricsMeasurement;
    }

    private DoubleMetric createDoubleMetric(final String id, final double value) {
        MetricDefinition metricDefinition = PMDMetricsProviderFactory.getSupportedMetrics()
                .stream()
                .filter(m -> m.getId().equals(id))
                .findFirst().get();

        return new DoubleMetric(metricDefinition, value);
    }

    private IntegerMetric createIntegerMetric(final String id, final int value) {
        MetricDefinition metricDefinition = PMDMetricsProviderFactory.getSupportedMetrics()
                .stream()
                .filter(m -> m.getId().equals(id))
                .findFirst().get();

        return new IntegerMetric(metricDefinition, value);
    }

}