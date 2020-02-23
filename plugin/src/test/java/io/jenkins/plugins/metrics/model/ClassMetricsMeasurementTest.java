package io.jenkins.plugins.metrics.model;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

import static org.assertj.core.api.Assertions.*;

public class ClassMetricsMeasurementTest {

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

    private Metric createIntMetric(final String id, final int value) {
        return new IntegerMetric(new MetricDefinition(id, "",
                "", "", 0, ArrayUtils.toArray(Scope.CLASS, Scope.METHOD)), value);
    }
}
