package io.jenkins.plugins.metrics.model.metric;

import edu.hm.hafner.util.Generated;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a metric that combines a {@link MetricDefinition} with a value. The specific type of the value is defined
 * by the generic type parameter T and is provided by subclasses.
 *
 * @param <T>
 *         the data type for the metric
 */
public abstract class Metric<T extends Number> implements Serializable {
    @Serial
    private static final long serialVersionUID = -8143304414028170807L;

    private final MetricDefinition metricDefinition;

    /**
     * Creates a new Metric instance.
     *
     * @param metricDefinition the metric definition to use
     */
    protected Metric(final MetricDefinition metricDefinition) {
        this.metricDefinition = metricDefinition;
    }

    /**
     * Return the value of this metric that should be displayed in the UI.
     *
     * @return the string to display
     */
    public abstract String renderValue();

    /**
     * Return the raw value of this metric, e.g., used for calculations.
     *
     * @return the raw value
     */
    public abstract T rawValue();

    public final String getId() {
        return metricDefinition.getId();
    }

    public final MetricDefinition getMetricDefinition() {
        return metricDefinition;
    }

    @Override
    public final String toString() {
        return "Metric %s: %s (%s)".formatted(getId(), renderValue(), rawValue());
    }

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var metric = (Metric<?>) o;
        return Objects.equals(metricDefinition, metric.metricDefinition);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hashCode(metricDefinition);
    }
}
