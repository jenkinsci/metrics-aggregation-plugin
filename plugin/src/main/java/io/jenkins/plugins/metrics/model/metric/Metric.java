package io.jenkins.plugins.metrics.model.metric;

import java.io.Serializable;

public abstract class Metric<T extends Number> implements Serializable {
    private static final long serialVersionUID = -8143304414028170807L;

    protected final MetricDefinition metricDefinition;

    protected Metric(final MetricDefinition metricDefinition) {
        this.metricDefinition = metricDefinition;
    }

    public abstract String renderValue();

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
