package io.jenkins.plugins.metrics.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class MetricsMeasurement implements Serializable {
    private static final long serialVersionUID = 7472039462715167623L;

    //TODO variables
    private String variableName = "";

    protected Map<Metric, Double> metrics = new HashMap<>();

    public void addMetric(final Metric metric, final double value) {
        metrics.put(metric, value);
    }

    public Map<Metric, Double> getMetrics() {
        return metrics;
    }

    public void setMetrics(final Map<Metric, Double> metrics) {
        this.metrics = metrics;
    }

    public Optional<Double> getMetric(final String id) {
        return Optional.ofNullable(metrics.get(new Metric(id)));
    }

    public abstract MetricsMeasurement merge(MetricsMeasurement metricsMeasurement);

    public abstract String getQualifiedClassName();
}
