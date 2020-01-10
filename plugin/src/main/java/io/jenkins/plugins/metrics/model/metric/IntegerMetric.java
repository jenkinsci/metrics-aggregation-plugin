package io.jenkins.plugins.metrics.model.metric;

import java.util.Objects;

public class IntegerMetric extends Metric<Integer> {
    private static final long serialVersionUID = 179851851088742850L;

    private int value;

    public IntegerMetric(final MetricDefinition metricDefinition, final int value) {
        super(metricDefinition);
        this.value = value;
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
    public boolean equals(final Object o) {
        if (!(o instanceof IntegerMetric)) {
            return false;
        }

        IntegerMetric other = (IntegerMetric) o;
        return Objects.equals(other.value, value) && Objects.equals(other.metricDefinition, metricDefinition);
    }
}
