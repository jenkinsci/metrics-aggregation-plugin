package io.jenkins.plugins.metrics.model.metric;

import java.io.Serial;
import java.util.Objects;

/**
 * A {@link Metric} representing a double value.
 */
public class DoubleMetric extends Metric<Double> {
    @Serial
    private static final long serialVersionUID = -7838111350390919589L;

    private final double value;

    /**
     * Constructor for a double metric.
     *
     * @param metricDefinition
     *         the definition of this metric
     * @param value
     *         the double value
     */
    public DoubleMetric(final MetricDefinition metricDefinition, final double value) {
        super(metricDefinition);

        this.value = value;
    }

    @Override
    public String renderValue() {
        return "%.2f".formatted(value);
    }

    @Override
    public Double rawValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DoubleMetric other)) {
            return false;
        }

        return Objects.equals(other.value, value) && Objects.equals(other.getMetricDefinition(), getMetricDefinition());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMetricDefinition(), value);
    }
}
