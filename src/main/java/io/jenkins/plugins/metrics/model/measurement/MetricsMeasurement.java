package io.jenkins.plugins.metrics.model.measurement;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import io.jenkins.plugins.metrics.model.metric.Metric;

/**
 * Measurement point of metrics. Combines the location of the measurement with the metrics measured there.
 */
public abstract class MetricsMeasurement implements Serializable {
    @Serial
    private static final long serialVersionUID = 7472039462715167623L;

    /**
     * The metrics reported for this measurement.
     */
    protected Map<String, Metric> metrics = new HashMap<>();
    /**
     * The file name of the measurement.
     */
    protected String fileName = "";
    /**
     * The package name of the measurement.
     */
    protected String packageName = "";
    /**
     * The class name of the measurement.
     */
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

    /**
     * Add a new {@link Metric}.
     *
     * @param metric
     *         the metric to add
     */
    public void addMetric(final Metric metric) {
        metrics.put(metric.getId(), metric);
    }

    public Map<String, Metric> getMetrics() {
        return metrics;
    }

    /**
     * Get a metric based on its id.
     *
     * @param id
     *         the id of the metric to look for
     *
     * @return an {@link Optional} containing the value of the metric, if it is present, an empty {@link Optional}
     *         otherwise
     */
    public Optional<Number> getMetric(final String id) {
        return Optional.ofNullable(metrics.get(id)).map(Metric::rawValue);
    }

    /**
     * Merge a {@link MetricsMeasurement} with this one.
     *
     * @param metricsMeasurement
     *         the {@link MetricsMeasurement} to merge
     *
     * @return the combined {@link MetricsMeasurement}
     */
    public abstract MetricsMeasurement merge(MetricsMeasurement metricsMeasurement);
}
