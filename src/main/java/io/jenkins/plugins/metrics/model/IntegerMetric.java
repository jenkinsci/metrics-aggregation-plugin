package io.jenkins.plugins.metrics.model;

import edu.hm.hafner.util.Generated;

import java.io.Serial;
import java.util.Objects;

/**
 * A {@link Metric} representing an integer value.
 */
public final class IntegerMetric extends Metric {
    @Serial
    private static final long serialVersionUID = 179851851088742850L;

    private final int value;

    /**
     * Creates an integer metric.
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
    public boolean needsRounding() {
        return true;
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
    @Generated
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        var that = (IntegerMetric) o;
        return value == that.value;
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }
}
