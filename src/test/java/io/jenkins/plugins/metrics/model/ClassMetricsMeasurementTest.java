package io.jenkins.plugins.metrics.model;

import org.junit.jupiter.api.Test;

import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement.ClassMetricsMeasurementBuilder;
import io.jenkins.plugins.metrics.model.metric.DoubleMetric;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.MetricDefinitionBuilder;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.model.metric.PercentageMetric;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for the class {@link ClassMetricsMeasurement}.
 */
class ClassMetricsMeasurementTest {
    private static final String INTEGER_ID = "INT-METRIC";
    private static final String DOUBLE_ID = "DOUBLE-METRIC";

    @Test
    void shouldAppendMetricsToClassMetric() {
        var builder = new ClassMetricsMeasurementBuilder();

        int intValue = 17;
        var doubleValue = 2.0;

        var single = builder.withMetric(new IntegerMetric(getMetricDefinition(INTEGER_ID), intValue)).build();

        assertThat(single.getMetric(INTEGER_ID)).isNotEmpty().hasValue(intValue);

        builder.withMetric(new IntegerMetric(getMetricDefinition(INTEGER_ID), intValue));
        builder.withMetric(new DoubleMetric(getMetricDefinition(DOUBLE_ID), doubleValue));

        var both = builder.build();

        assertThat(both.getMetric(INTEGER_ID)).isNotEmpty().hasValue(intValue);
        assertThat(both.getMetric(DOUBLE_ID)).isNotEmpty().hasValue(doubleValue);
    }

    @Test
    void shouldMergeClassMetric() {
        var measurement = build();

        var empty = new ClassMetricsMeasurementBuilder().build();
        assertThat(empty.getMetrics()).isEmpty();

        empty.merge(measurement);
        assertThat(empty.getMetrics()).hasSize(1);
    }

    private ClassMetricsMeasurement build() {
        var builder = new ClassMetricsMeasurementBuilder();

        return builder.withMetric(new PercentageMetric(getMetricDefinition(INTEGER_ID), 100)).build();
    }

    private MetricDefinition getMetricDefinition(final String id) {
        return new MetricDefinitionBuilder(id)
                .withScopes(Scope.CLASS, Scope.METHOD)
                .build();
    }
}
