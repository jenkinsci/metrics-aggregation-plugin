package io.jenkins.plugins.metrics.model.measurement;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.jenkins.plugins.metrics.model.metric.Metric;

public abstract class MetricsMeasurement implements Serializable {
    private static final long serialVersionUID = 7472039462715167623L;

    protected Map<String, Metric> metrics = new HashMap<>();
    protected String fileName = "";
    protected String packageName = "";
    protected String className = "";

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(final String className) {
        this.className = className;
    }

    public String getQualifiedClassName() {
        return this.packageName + '.' + this.className;
    }

    public void addMetric(final Metric metric) {
        metrics.put(metric.getId(), metric);
    }

    public Map<String, Metric> getMetrics() {
        return metrics;
    }

    public Optional<Number> getMetric(final String id) {
        return Optional.ofNullable(metrics.get(id)).map(Metric::rawValue);
    }

    public abstract MetricsMeasurement merge(MetricsMeasurement metricsMeasurement);    
}
