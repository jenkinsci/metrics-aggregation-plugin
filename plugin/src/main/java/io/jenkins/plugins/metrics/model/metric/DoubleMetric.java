package io.jenkins.plugins.metrics.model.metric;

import java.util.Objects;

public class DoubleMetric extends Metric<Double> {
    private static final long serialVersionUID = -7838111350390919589L;

    private double value;

    public DoubleMetric(final MetricDefinition metricDefinition, final double value) {
        super(metricDefinition);
        this.value = value;
    }

    @Override
    public String renderValue() {
        return String.format("%.2f", value);
    }

    @Override
    public Double rawValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof DoubleMetric)) {
            return false;
        }

        DoubleMetric other = (DoubleMetric) o;
        return Objects.equals(other.value, value) && Objects.equals(other.metricDefinition, metricDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricDefinition, value);
    }
}
