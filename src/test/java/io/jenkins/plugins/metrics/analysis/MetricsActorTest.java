package io.jenkins.plugins.metrics.analysis;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import hudson.util.StreamTaskListener;

import io.jenkins.plugins.metrics.extension.PMDMetricsProviderFactory;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.DoubleMetric;
import io.jenkins.plugins.metrics.model.IntegerMetric;
import io.jenkins.plugins.metrics.model.MetricDefinition;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;

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