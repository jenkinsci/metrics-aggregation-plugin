package io.jenkins.plugins.metrics.model.metric;

import java.io.Serial;
import java.util.Objects;

/**
 * A {@link Metric} representing an integer value.
 */
public class IntegerMetric extends Metric<Integer> {
    @Serial
    private static final long serialVersionUID = 179851851088742850L;

    private final int value;

    /**
     * Constructor for a integer metric.
     *
     * @param metricDefinition
     *         the definition of this metric
     * @param value
     *         the integer value
     */
    public IntegerMetric(final MetricDefinition metricDefinition, final int value) {
        super(metricDefinition);

        this.value = value;
    }

    @Override
    public String renderValue() {
        return String.valueOf(value);
    }

    @Override
    public Integer rawValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof IntegerMetric other)) {
            return false;
        }

        return Objects.equals(other.value, value) && Objects.equals(other.metricDefinition, metricDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricDefinition, value);
    }
}
