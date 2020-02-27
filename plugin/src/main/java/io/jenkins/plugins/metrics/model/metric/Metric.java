package io.jenkins.plugins.metrics.model.metric;

import java.io.Serializable;

/**
 * This class represents a metric. It combines a {@link MetricDefinition} with a value for this metric.
 *
 * @param <T>
 *         the data type for the metric
 */
public abstract class Metric<T extends Number> implements Serializable {
    private static final long serialVersionUID = -8143304414028170807L;

    /**
     * The definition for this metric.
     */
    protected final MetricDefinition metricDefinition;

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
     * Return the raw value of this metric, e.g. used for calculations.
     *
     * @return the raw value
     */
    public abstract T rawValue();

    public String getId() {
        return metricDefinition.getId();
    }

    public MetricDefinition getMetricDefinition() {
        return metricDefinition;
    }

    @Override
    public String toString() {
        return String.format("Metric %s: %s (%s)", getId(), renderValue(), rawValue());
    }
}
