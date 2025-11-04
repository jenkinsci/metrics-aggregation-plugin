package io.jenkins.plugins.metrics.model.metric;

import edu.hm.hafner.util.Generated;

import java.io.Serial;
import java.util.Objects;

/**
 * {@link Metric} for percentage metrics.
 */
public class PercentageMetric extends Metric<Float> {
    @Serial
    private static final long serialVersionUID = -239297826452518258L;

    private final float value;

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
        return "%d%%".formatted(Math.round(value));
    }

    @Override
    public Float rawValue() {
        return value;
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var that = (PercentageMetric) o;
        return Float.compare(value, that.value) == 0;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
