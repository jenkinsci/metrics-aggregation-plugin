package io.jenkins.plugins.metrics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClassMetricsMeasurement extends MetricsMeasurement {
    private static final long serialVersionUID = 6801327926336683068L;

    private String fileName = "";
    private String packageName = "";
    private String className = "";
    private List<MetricsMeasurement> children = new ArrayList<>();

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

    public List<MetricsMeasurement> getChildren() {
        return children;
    }

    public void setChildren(final List<MetricsMeasurement> children) {
        this.children = children;
    }

    public void addChild(final MetricsMeasurement child) {
        this.children.add(child);
    }

    /**
     * Merge another {@link MetricsMeasurement} with this one. Returns itself to be usable for chaining.
     *
     * @param metricsMeasurement
     *         the {@link MetricsMeasurement} to merge. If it is a {@link MethodMetricsMeasurement}, it is added as
     *         child, if it is a {@link ClassMetricsMeasurement}, their metrics are merged together.
     *
     * @return itself
     */
    @Override
    public ClassMetricsMeasurement merge(final MetricsMeasurement metricsMeasurement) {
        if (metricsMeasurement instanceof ClassMetricsMeasurement) {
            metrics.addAll(metricsMeasurement.getMetrics());
        }
        else if (metricsMeasurement instanceof MethodMetricsMeasurement) {
            addChild(metricsMeasurement);
        }
        return this;
    }

    @Override
    public String getQualifiedClassName() {
        return packageName + "." + className;
    }

    @Override
    public String toString() {
        if (this.equals(new ClassMetricsMeasurement())) {
            return "ClassMetricsMeasurement[empty]";
        }

        return String.format("ClassMetricsMeasurement[%s.%s], %d children",
                packageName,
                className,
                children.size());
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
                && Objects.equals(metrics, other.metrics)
                && Objects.equals(children, other.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, packageName, className, children, metrics);
    }
}
