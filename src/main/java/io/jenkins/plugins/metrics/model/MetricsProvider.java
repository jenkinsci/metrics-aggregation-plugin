package io.jenkins.plugins.metrics.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MetricsProvider {

    private String origin;
    private List<MetricsMeasurement> metricsMeasurements = new ArrayList<>();
    private Set<Metric> projectSummary = new HashSet<>();

    public List<MetricsMeasurement> getMetricsMeasurements() {
        return metricsMeasurements;
    }

    public void setMetricsMeasurements(final List<MetricsMeasurement> metricsMeasurements) {
        this.metricsMeasurements = metricsMeasurements;
    }

    public Set<Metric> getProjectSummary() {
        return projectSummary;
    }

    public void setProjectSummary(final Set<Metric> projectSummary) {
        this.projectSummary = projectSummary;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(final String origin) {
        this.origin = origin;
    }
}
