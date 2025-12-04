package io.jenkins.plugins.metrics.model;

import edu.hm.hafner.util.Generated;

import java.io.Serial;
import java.util.Locale;
import java.util.Objects;

/**
 * {@link Metric} for percentage metrics.
 */
public final class PercentageMetric extends Metric {
    @Serial
    private static final long serialVersionUID = -239297826452518258L;

    private final double value;

    /**
     * Creates a new percentage metric.
     *
     * @param metricDefinition
     *         the definition of this metric
     * @param value
     *         the percentage value as float
     */
    public PercentageMetric(final MetricDefinition metricDefinition, final double value) {
        super(metricDefinition);

        this.value = value;
    }

    @Override
    public String renderValue() {
        return String.format(Locale.ENGLISH, "%d%%", Math.round(value));
    }

    @Override
    public Double rawValue() {
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
        return Double.compare(value, that.value) == 0;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
