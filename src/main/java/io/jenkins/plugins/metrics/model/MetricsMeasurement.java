package io.jenkins.plugins.metrics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public abstract class MetricsMeasurement implements Serializable {
    private static final long serialVersionUID = 7472039462715167623L;

    //TODO variables
    private String variableName = "";

    protected Set<Metric> metrics = new HashSet<>();

    public void addMetric(final Metric metric) {
        metrics.add(metric);
    }

    public Set<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(final Set<Metric> metrics) {
        this.metrics = metrics;
    }

    public abstract MetricsMeasurement merge(MetricsMeasurement metricsMeasurement);

    public abstract String getQualifiedClassName();
}
