package io.jenkins.plugins.metrics.model.measurement;

import java.io.Serial;
import java.util.Objects;

/**
 * {@link MetricsMeasurement} representing one class.
 */
public class ClassMetricsMeasurement extends MetricsMeasurement {
    @Serial
    private static final long serialVersionUID = 6801327926336683068L;

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
            metrics.putAll(metricsMeasurement.getMetrics());
        }
        return this;
    }

    @Override
    public String toString() {
        if (this.equals(new ClassMetricsMeasurement())) {
            return "ClassMetricsMeasurement[empty]";
        }

        return "ClassMetricsMeasurement[%s.%s; %s]".formatted(packageName, className, fileName);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ClassMetricsMeasurement other)) {
            return false;
        }

        return Objects.equals(fileName, other.fileName)
                && Objects.equals(packageName, other.packageName)
                && Objects.equals(className, other.className)
                && Objects.equals(metrics, other.metrics);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, packageName, className, metrics);
    }
}
