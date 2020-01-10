package io.jenkins.plugins.metrics.model;

import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.util.JacksonFacade;

import static org.assertj.core.api.Assertions.*;

public class ClassMetricsMeasurementTest {

    private final JacksonFacade jacksonFacade = new JacksonFacade();

    @Test
    public void shouldSerializeToJson() {
        final String className = "MyTestClass";
        final String packageName = "my.test.package";
        final String fileName = "MyTestClass.java";
        final Metric metric = createIntMetric("TESTMETRIC", 17);

        final ClassMetricsMeasurement measurement = new ClassMetricsMeasurement();
        measurement.setFileName(fileName);
        measurement.setClassName(className);
        measurement.setPackageName(packageName);

        measurement.addMetric(metric);

        final String json = jacksonFacade.toJson(measurement);

        assertThat(json).contains(formatJsonValue("fileName", fileName));
        assertThat(json).contains(formatJsonValue("className", className));
        assertThat(json).contains(formatJsonValue("packageName", packageName));
        assertThat(json).contains(formatJsonValue("qualifiedClassName", packageName + "." + className));
        assertThat(json).contains(formatJsonValue("metricsRaw", Maps.newHashMap(metric.getId(), metric.rawValue())));
        assertThat(json).contains(
                formatJsonValue("metricsDisplay", Maps.newHashMap(metric.getId(), metric.renderValue())));
    }

    @Test
    public void shouldSerializeMultipleToJson() {
        List<ClassMetricsMeasurement> measurements = Lists.newArrayList(new ClassMetricsMeasurement(),
                new ClassMetricsMeasurement(), new ClassMetricsMeasurement());

        final String json = jacksonFacade.toJson(measurements);

        assertThat(json).contains("lalal");
    }

    @Test
    public void shouldGetMetric() {
        final String metricId = "TESTMETRIC";
        final int metricValue = 17;
        final Metric metric = createIntMetric(metricId, metricValue);
        final ClassMetricsMeasurement measurement = new ClassMetricsMeasurement();
        measurement.addMetric(metric);

        assertThat(measurement.getMetric(metricId)).isNotEmpty();
        assertThat(measurement.getMetric(metricId)).hasValue(metricValue);
    }

    private String formatJsonValue(final String key, final Object value) {
        return String.format("\"%s\":%s", key, jacksonFacade.toJson(value));
    }

    private Metric createIntMetric(final String id, final int value) {
        return new IntegerMetric(new MetricDefinition(id, "",
                "", "", 0, ArrayUtils.toArray(Scope.CLASS, Scope.METHOD)), value);
    }
}
