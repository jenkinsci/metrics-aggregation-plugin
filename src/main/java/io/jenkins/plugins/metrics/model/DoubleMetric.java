package io.jenkins.plugins.metrics.model;

import edu.hm.hafner.util.Generated;

import java.io.Serial;
import java.util.Locale;
import java.util.Objects;

/**
 * A {@link Metric} representing a double value.
 */
public final class DoubleMetric extends Metric {
    @Serial
    private static final long serialVersionUID = -7838111350390919589L;

    private final double value;

    /**
     * Creates a new double metric.
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
        return String.format(Locale.ENGLISH, "%.2f", value);
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
        var that = (DoubleMetric) o;
        return Double.compare(value, that.value) == 0;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
