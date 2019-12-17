package io.jenkins.plugins.metrics.model;

public class PercentageMetric extends Metric<Float> {
    private static final long serialVersionUID = -239297826452518258L;

    private float value;

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
}
