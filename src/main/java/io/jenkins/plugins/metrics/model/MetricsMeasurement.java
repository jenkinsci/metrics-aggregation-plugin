package io.jenkins.plugins.metrics.model;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import edu.hm.hafner.util.Generated;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Measurement point of metrics. Combines the location of the measurement with the metrics measured there.
 */
public abstract class MetricsMeasurement implements Serializable {
    @Serial
    private static final long serialVersionUID = 7472039462715167623L;

    @SuppressWarnings("PMD.LooseCoupling")
    private final HashMap<String, Metric> metrics;
    private final String fileName;
    private final String packageName;
    private final String className;

    /**
     * Creates a new empty {@link MetricsMeasurement}.
     *
     * @param metrics
     *         the metrics measured
     * @param fileName
     *         the file name where the measurement was taken
     * @param packageName
     *         the package name where the measurement was taken
     * @param className
     *         the class name where the measurement was taken
     */
    protected MetricsMeasurement(final Map<String, Metric> metrics, final String fileName, final String packageName,
            final String className) {
        this.metrics = new HashMap<>(metrics);
        this.fileName = fileName;
        this.packageName = packageName;
        this.className = className;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public String getQualifiedClassName() {
        return this.getPackageName() + '.' + this.getClassName();
    }

    /**
     * The metrics reported for this measurement.
     *
     * @return the metrics mapping from metric id to metric
     */
    public Map<String, Metric> getMetrics() {
        return Map.copyOf(metrics);
    }

    /**
     * Merges the given metric into this measurement.
     *
     * @param metric
     *         the metric to merge
     */
    protected void merge(final Metric metric) {
        if (metrics.containsKey(metric.getId())) {
            throw new IllegalArgumentException("Metric with id '%s' is already present".formatted(metric.getId()));
        }
        this.metrics.put(metric.getId(), metric);
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
        return Optional.ofNullable(getMetrics().get(id)).map(Metric::rawValue);
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

    @Override
    @Generated
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (MetricsMeasurement) o;
        return Objects.equals(metrics, that.metrics)
                && Objects.equals(fileName, that.fileName)
                && Objects.equals(packageName, that.packageName)
                && Objects.equals(className, that.className);
    }

    @Override
    @Generated
    public int hashCode() {
        return Objects.hash(metrics, fileName, packageName, className);
    }

    /**
     * Base builder for {@link MetricsMeasurement} instances.
     *
     * @author Ullrich Hafner
     * @param <T> the type of the builder
     */
    @SuppressWarnings({"unchecked", "checkstyle:HiddenField", "ParameterHidesMemberVariable"})
    public abstract static class MetricsMeasurementBuilder<T extends MetricsMeasurementBuilder<T>> {
        private final Map<String, Metric> metrics = new HashMap<>();
        private String fileName;
        private String packageName;
        private String className;

        public Map<String, Metric> getMetrics() {
            return metrics;
        }

        /**
         * Sets the metrics for the metrics measurement.
         *
         * @param metric
         *         the new metric to add
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public T withMetric(final Metric metric) {
            if (this.metrics.containsKey(metric.getId())) {
                throw new IllegalArgumentException("Metric with id '%s' is already present".formatted(metric.getId()));
            }
            this.metrics.put(metric.getId(), metric);

            return (T) this;
        }

        public String getFileName() {
            return fileName;
        }

        /**
         * Sets the file name for the metrics measurement.
         *
         * @param fileName
         *         the file name
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public T withFileName(final String fileName) {
            this.fileName = fileName;

            return (T) this;
        }

        public String getPackageName() {
            return packageName;
        }

        /**
         * Sets the package name for the metrics measurement.
         *
         * @param packageName
         *         the package name
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public T withPackageName(final String packageName) {
            this.packageName = packageName;

            return (T) this;
        }

        public String getClassName() {
            return className;
        }


        /**
         * Sets the class name for the metrics measurement.
         *
         * @param className
         *         the class name
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public T withClassName(final String className) {
            this.className = className;

            return (T) this;
        }

        /**
         * Builds the {@link MetricsMeasurement} instance.
         *
         * @return the built {@link MetricsMeasurement} instance
         */
        public abstract MetricsMeasurement build();
    }
}
