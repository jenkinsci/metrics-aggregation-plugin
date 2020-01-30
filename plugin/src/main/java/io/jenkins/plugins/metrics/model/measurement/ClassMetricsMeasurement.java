package io.jenkins.plugins.metrics.model.measurement;

import java.util.Objects;

public class ClassMetricsMeasurement extends MetricsMeasurement {
    private static final long serialVersionUID = 6801327926336683068L;

    /**
     * Merge another {@link MetricsMeasurement} with this one. Returns itself to be usable for chaining.
     *
     * @param metricsMeasurement
     *         the {@link MetricsMeasurement} to merge. If it is a {@link ClassMetricsMeasurement}, their metrics are
     *         merged together. In case of a {@link MethodMetricsMeasurement} nothing happens.
     *
     * @return itself
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

        return String.format("ClassMetricsMeasurement[%s.%s]", packageName, className);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof ClassMetricsMeasurement)) {
            return false;
        }

        ClassMetricsMeasurement other = (ClassMetricsMeasurement) o;

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
