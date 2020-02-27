package io.jenkins.plugins.metrics.model.metric;

import java.util.Objects;

/**
 * {@link Metric} for percentage metrics.
 */
public class PercentageMetric extends Metric<Float> {
    private static final long serialVersionUID = -239297826452518258L;

    private float value;

    /**
     * Constructor for a percentage metric.
     *
     * @param metricDefinition
     *         the definition of this metric
     * @param value
     *         the percentage value as float
     */
    public PercentageMetric(final MetricDefinition metricDefinition, final float value) {
        super(metricDefinition);
        this.value = value;
    }

    @Override
    public String renderValue() {
        return String.format("%d%%", Math.round(value));
    }

    @Override
    public Float rawValue() {
        return value;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof PercentageMetric)) {
            return false;
        }

        PercentageMetric other = (PercentageMetric) o;
        return Objects.equals(other.value, value) && Objects.equals(other.metricDefinition, metricDefinition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metricDefinition, value);
    }
}
