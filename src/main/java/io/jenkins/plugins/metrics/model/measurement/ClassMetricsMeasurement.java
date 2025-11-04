package io.jenkins.plugins.metrics.model.measurement;

import java.io.Serial;
import java.util.Map;

import io.jenkins.plugins.metrics.model.metric.Metric;

/**
 * {@link MetricsMeasurement} representing one class.
 */
public final class ClassMetricsMeasurement extends MetricsMeasurement {
    @Serial
    private static final long serialVersionUID = 6801327926336683068L;

    private ClassMetricsMeasurement(final Map<String, Metric> metrics, final String fileName,
            final String packageName, final String className) {
        super(metrics, fileName, packageName, className);
    }

    /**
     * Merge another {@link MetricsMeasurement} with this one. Returns this to be usable for chaining.
     *
     * @param metricsMeasurement
     *         the {@link MetricsMeasurement} to merge. If it is a {@link ClassMetricsMeasurement}, their metrics are
     *         merged. In case of a {@link MethodMetricsMeasurement} nothing happens.
     *
     * @return this
     */
    @Override
    public ClassMetricsMeasurement merge(final MetricsMeasurement metricsMeasurement) {
        if (metricsMeasurement instanceof ClassMetricsMeasurement) {
            getMetrics().putAll(metricsMeasurement.getMetrics());
        }
        return this;
    }

    @Override
    public String toString() {
        return "ClassMetricsMeasurement[%s.%s; %s]".formatted(getPackageName(), getClassName(), getFileName());
    }

    /**
     * Builder for {@link ClassMetricsMeasurement} instances.
     */
    public static class ClassMetricsMeasurementBuilder extends MetricsMeasurementBuilder<ClassMetricsMeasurementBuilder> {
        /**
         * Builds the {@link ClassMetricsMeasurement} instance.
         *
         * @return the built {@link ClassMetricsMeasurement} instance
         */
        public ClassMetricsMeasurement build() {
            return new ClassMetricsMeasurement(getMetrics(), getFileName(), getPackageName(), getClassName());
        }
    }
}
