package io.jenkins.plugins.metrics.model;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement.ClassMetricsMeasurementBuilder;
import io.jenkins.plugins.metrics.model.MethodMetricsMeasurement.MethodMetricsMeasurementBuilder;
import io.jenkins.plugins.metrics.model.MetricDefinition.MetricDefinitionBuilder;
import io.jenkins.plugins.metrics.model.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.model.MetricsMeasurement.MetricsMeasurementBuilder;

import static org.assertj.core.api.Assertions.*;

/**
 * Test for the class {@link ClassMetricsMeasurement}.
 */
class MetricsMeasurementTest {
    private static final String INTEGER_ID = "INT-METRIC";
    private static final String DOUBLE_ID = "DOUBLE-METRIC";

    @ParameterizedTest
    @MethodSource("getMetrics")
    void shouldAppendMetricsToClassMetric(final MetricsMeasurementBuilder<?> builder) {
        int intValue = 17;
        var doubleValue = 2.0;

        var single = builder.withMetric(new IntegerMetric(getMetricDefinition(INTEGER_ID), intValue)).build();

        assertThat(single.getMetric(INTEGER_ID)).isNotEmpty().hasValue(intValue);

        builder.withMetric(new DoubleMetric(getMetricDefinition(DOUBLE_ID), doubleValue));

        var both = builder.build();

        assertThat(both.getMetric(INTEGER_ID)).isNotEmpty().hasValue(intValue);
        assertThat(both.getMetric(DOUBLE_ID)).isNotEmpty().hasValue(doubleValue);
    }

    static Stream<Arguments> getMetrics() {
        return Stream.of(
                Arguments.of(
                        Named.of(ClassMetricsMeasurementBuilder.class.getSimpleName(),
                                new ClassMetricsMeasurementBuilder()),
                        Named.of(MethodMetricsMeasurementBuilder.class.getSimpleName(),
                                new MethodMetricsMeasurementBuilder())),
                Arguments.of(
                        Named.of(MethodMetricsMeasurementBuilder.class.getSimpleName(),
                                new MethodMetricsMeasurementBuilder()),
                        Named.of(ClassMetricsMeasurementBuilder.class.getSimpleName(),
                                new ClassMetricsMeasurementBuilder())));
    }

    @ParameterizedTest
    @MethodSource("getMetrics")
    void shouldMergeClassMetric(final MetricsMeasurementBuilder<?> builder,
            final MetricsMeasurementBuilder<?> wrongBuilder) {
        var empty = builder.build();

        var measurement = builder.withMetric(new PercentageMetric(getMetricDefinition(INTEGER_ID), 100)).build();

        assertThat(empty.getMetrics()).isEmpty();

        empty.merge(measurement);
        assertThat(empty.getMetrics()).hasSize(1);

        var wrongType = wrongBuilder.build();
        assertThat(wrongType.getMetrics()).isEmpty();

        wrongType.merge(measurement); // skips merging
        assertThat(wrongType.getMetrics()).isEmpty();
    }

    private MetricDefinition getMetricDefinition(final String id) {
        return new MetricDefinitionBuilder(id)
                .withScopes(Scope.CLASS, Scope.METHOD)
                .build();
    }
}
