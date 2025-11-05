package io.jenkins.plugins.metrics.model;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.Serial;
import java.util.Map;
import java.util.Objects;

/**
 * {@link MetricsMeasurement} for a method.
 */
public class MethodMetricsMeasurement extends MetricsMeasurement {
    @Serial
    private static final long serialVersionUID = 6103621887323104682L;

    private final String methodName;
    private final int beginLine;
    private final int beginColumn;
    private final int endLine;
    private final int endColumn;

    @SuppressWarnings("checkstyle:ParameterNumber")
    private MethodMetricsMeasurement(final Map<String, Metric> metrics,
            final String fileName, final String packageName, final String className, final String methodName,
            final int beginLine, final int beginColumn, final int endLine, final int endColumn) {
        super(metrics, fileName, packageName, className);

        this.methodName = methodName;
        this.beginLine = beginLine;
        this.beginColumn = beginColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getBeginLine() {
        return beginLine;
    }

    public int getBeginColumn() {
        return beginColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    /**
     * Merge another {@link MetricsMeasurement} with this one. Returns this to be usable for chaining.
     *
     * @param metricsMeasurement
     *         the {@link MetricsMeasurement} to merge. If it is a {@link MethodMetricsMeasurement},their metrics are
     *         merged together, if it is a {@link ClassMetricsMeasurement}, nothing happens.
     *
     * @return itself
     */
    @Override
    public MethodMetricsMeasurement merge(final MetricsMeasurement metricsMeasurement) {
        if (metricsMeasurement instanceof MethodMetricsMeasurement) {
            metricsMeasurement.getMetrics().values().forEach(this::merge);
        }
        return this;
    }

    @Override
    public String toString() {
        return "MethodMetricsMeasurement[%s.%s#%s:%d:%d]".formatted(
                getPackageName(),
                getClassName(),
                methodName,
                beginLine,
                endLine);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MethodMetricsMeasurement other)) {
            return false;
        }

        return this.beginLine == other.beginLine
                && this.beginColumn == other.beginColumn
                && this.endLine == other.endLine
                && this.endColumn == other.endColumn
                && Objects.equals(methodName, other.methodName)
                && Objects.equals(getMetrics(), other.getMetrics())
                && Objects.equals(getClassName(), other.getClassName())
                && Objects.equals(getFileName(), other.getFileName())
                && Objects.equals(getPackageName(), other.getPackageName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(beginColumn, beginLine, endLine, endColumn, methodName,
                getClassName(), getFileName(), getPackageName(), getMetrics());
    }

    /**
     * Builder for {@link MethodMetricsMeasurement} instances.
     */
    @SuppressWarnings({"checkstyle:HiddenField", "ParameterHidesMemberVariable"})
    public static class MethodMetricsMeasurementBuilder
            extends MetricsMeasurementBuilder<MethodMetricsMeasurementBuilder> {
        private String methodName;
        private int beginLine;
        private int beginColumn;
        private int endLine;
        private int endColumn;

        /**
         * Sets the method name.
         *
         * @param methodName
         *         the method name
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MethodMetricsMeasurementBuilder withMethodName(final String methodName) {
            this.methodName = methodName;

            return this;
        }

        /**
         * Sets the first line for the metric.
         *
         * @param beginLine
         *         the first line
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MethodMetricsMeasurementBuilder withBeginLine(final int beginLine) {
            this.beginLine = beginLine;

            return this;
        }

        /**
         * Sets the last line for the metric.
         *
         * @param endLine
         *         the last line
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MethodMetricsMeasurementBuilder withEndLine(final int endLine) {
            this.endLine = endLine;

            return this;
        }

        /**
         * Sets the first column for the metric.
         *
         * @param beginColumn
         *         the first column
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MethodMetricsMeasurementBuilder withBeginColumn(final int beginColumn) {
            this.beginColumn = beginColumn;

            return this;
        }

        /**
         * Sets the last column for the metric.
         *
         * @param endColumn
         *         the last column
         *
         * @return the builder instance
         */
        @CanIgnoreReturnValue
        public MethodMetricsMeasurementBuilder withEndColumn(final int endColumn) {
            this.endColumn = endColumn;

            return this;
        }

        /**
         * Builds the {@link MethodMetricsMeasurement}.
         *
         * @return the built {@link MethodMetricsMeasurement}
         */
        @Override
        public MethodMetricsMeasurement build() {
            return new MethodMetricsMeasurement(getMetrics(), getFileName(), getPackageName(), getClassName(),
                    methodName, beginLine, beginColumn, endLine, endColumn);
        }
    }
}
