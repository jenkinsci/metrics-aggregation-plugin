package io.jenkins.plugins.metrics.model;

import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MethodMetricsMeasurement;
import io.jenkins.plugins.metrics.util.JacksonFacade;

import static org.assertj.core.api.Assertions.*;

public class MethodMetricsMeasurementTest {

    private final JacksonFacade jacksonFacade = new JacksonFacade();

    @Test
    public void shouldCorrectlySerializeToJson() {
        final int beginLine = 1;
        final int endLine = 2;
        final int beginColumn = 3;
        final int endColumn = 4;
        final String methodName = "myTestMethod";
        final String className = "MyTestClass";
        final String packageName = "my.test.package";
        final String fileName = "MyTestClass.java";
        final Metric metric = createIntMetric("TESTMETRIC", 17);

        final MethodMetricsMeasurement measurement = new MethodMetricsMeasurement();
        measurement.setBeginLine(beginLine);
        measurement.setEndLine(endLine);
        measurement.setBeginColumn(beginColumn);
        measurement.setEndColumn(endColumn);
        measurement.setMethodName(methodName);

        final ClassMetricsMeasurement parent = getParent(fileName, className, packageName);
        measurement.setParent(parent);

        measurement.addMetric(metric);

        final String json = jacksonFacade.toJson(measurement);

        assertThat(json).contains(formatJsonValue("beginLine", beginLine));
        assertThat(json).contains(formatJsonValue("beginColumn", beginColumn));
        assertThat(json).contains(formatJsonValue("endLine", endLine));
        assertThat(json).contains(formatJsonValue("endColumn", endColumn));
        assertThat(json).contains(formatJsonValue("methodName", methodName));
        assertThat(json).contains(formatJsonValue("qualifiedClassName", packageName + "." + className));
        assertThat(json).contains(formatJsonValue("parent", parent));
        assertThat(json).contains(formatJsonValue("metricsRaw", Maps.newHashMap(metric.getId(), metric.rawValue())));
        assertThat(json).contains(
                formatJsonValue("metricsDisplay", Maps.newHashMap(metric.getId(), metric.renderValue())));
    }

    @Test
    public void shouldMergeTwoMetricsMeasurements() {
        final MethodMetricsMeasurement measurement2 = new MethodMetricsMeasurement();
        final Metric metric = createIntMetric("TESTMETRIC", 17);
        measurement2.addMetric(metric);

        final MethodMetricsMeasurement measurement1 = new MethodMetricsMeasurement();

        assertThat(measurement1.getMetrics()).isEmpty();
        measurement1.merge(measurement2);
        assertThat(measurement1.getMetrics()).containsValue(metric);
    }

    @Test
    public void shouldGetMetric() {
        final String metricId = "TESTMETRIC";
        final int metricValue = 17;
        final Metric metric = createIntMetric(metricId, metricValue);
        final MethodMetricsMeasurement measurement = new MethodMetricsMeasurement();
        measurement.addMetric(metric);

        assertThat(measurement.getMetric(metricId)).isNotEmpty();
        assertThat(measurement.getMetric(metricId)).hasValue(metricValue);
    }

    private String formatJsonValue(final String key, final Object value) {
        return String.format("\"%s\":%s", key, jacksonFacade.toJson(value));
    }

    private ClassMetricsMeasurement getParent(final String fileName, final String className, final String packageName) {
        final ClassMetricsMeasurement classMetricsMeasurement = new ClassMetricsMeasurement();
        classMetricsMeasurement.setFileName(fileName);
        classMetricsMeasurement.setClassName(className);
        classMetricsMeasurement.setPackageName(packageName);

        return classMetricsMeasurement;
    }

    private Metric createIntMetric(final String id, final int value) {
        return new IntegerMetric(new MetricDefinition(id, "",
                "", "", 0, ArrayUtils.toArray(Scope.CLASS, Scope.METHOD)), value);
    }
}
