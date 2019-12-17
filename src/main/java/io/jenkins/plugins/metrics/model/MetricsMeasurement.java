package io.jenkins.plugins.metrics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.ObjectUtils.Null;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class MetricsMeasurement implements Serializable {
    private static final long serialVersionUID = 7472039462715167623L;

    //TODO variables?
    private String variableName = "";

    @JsonIgnore
    protected Map<String, Metric> metrics = new HashMap<>();

    public void addMetric(final Metric metric) {
        metrics.put(metric.getId(), metric);
    }

    public Map<String, Metric> getMetrics() {
        return metrics;
    }

    @JsonGetter("metricsRaw")
    public Map<String, Number> getMetricsRaw() {
        return metrics.values().stream().collect(Collectors.toMap(Metric::getId, Metric::rawValue));
    }

    @JsonGetter("metricsDisplay")
    public Map<String, String> getMetricsDisplay() {
        return metrics.values().stream().collect(Collectors.toMap(Metric::getId, Metric::renderValue));
    }

    public Optional<Number> getMetric(final String id) {
        return Optional.ofNullable(metrics.get(id)).map(Metric::rawValue);
    }

    public abstract MetricsMeasurement merge(MetricsMeasurement metricsMeasurement);

    public abstract String getQualifiedClassName();
}
