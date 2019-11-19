package io.jenkins.plugins.metrics.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MetricsMeasurement implements Serializable {
    private static final long serialVersionUID = 7472039462715167623L;

    public static final MetricsMeasurement EMPTY_MEASUREMENT = new MetricsMeasurement();

    private String fileName = "";
    private int beginLine = -1;
    private int beginColumn = -1;
    private int endLine = -1;
    private int endColumn = -1;
    private String packageName = "";
    private String className = "";
    private String methodName = "";
    private String variableName = "";
    private List<MetricsMeasurement> children = new ArrayList<>();

    private Map<String, Double> metrics = new HashMap<>();

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public int getBeginLine() {
        return beginLine;
    }

    public void setBeginLine(final int beginLine) {
        this.beginLine = beginLine;
    }

    public int getBeginColumn() {
        return beginColumn;
    }

    public void setBeginColumn(final int beginColumn) {
        this.beginColumn = beginColumn;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(final int endLine) {
        this.endLine = endLine;
    }

    public int getEndColumn() {
        return endColumn;
    }

    public void setEndColumn(final int endColumn) {
        this.endColumn = endColumn;
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

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(final String methodName) {
        this.methodName = methodName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(final String variableName) {
        this.variableName = variableName;
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public void setMetrics(final Map<String, Double> metrics) {
        this.metrics = metrics;
    }

    public void addMetric(String metric, double value) {
        this.metrics.put(metric, value);
    }

    public List<MetricsMeasurement> getChildren() {
        return children;
    }

    public void setChildren(final List<MetricsMeasurement> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        if (this.equals(EMPTY_MEASUREMENT)) {
            return "MetricsMeasurement[empty]";
        }

        return String.format("MetricsMeasurement[%s.%s#%s:%d:%d], %d children",
                packageName,
                className,
                methodName,
                beginLine,
                endLine,
                children.size());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof MetricsMeasurement)) {
            return false;
        }

        MetricsMeasurement other = (MetricsMeasurement) o;

        return Objects.equals(fileName, other.fileName)
                && this.beginLine == other.beginLine
                && this.beginColumn == other.beginColumn
                && this.endLine == other.endLine
                && this.endColumn == other.endColumn
                && Objects.equals(packageName, other.packageName)
                && Objects.equals(className, other.className)
                && Objects.equals(methodName, other.methodName)
                && Objects.equals(variableName, other.variableName)
                && Objects.equals(children, other.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, beginColumn, beginLine, endLine, endColumn, packageName, className,
                methodName, variableName, children);
    }
}
