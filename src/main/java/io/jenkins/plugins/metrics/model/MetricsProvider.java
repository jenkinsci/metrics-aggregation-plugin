package io.jenkins.plugins.metrics.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricsProvider {

    private String origin;
    private List<MetricsMeasurement> metricsMeasurements = new ArrayList<>();
    private Map<Metric, Double> projectSummary = new HashMap<>();

    public List<MetricsMeasurement> getMetricsMeasurements() {
        return metricsMeasurements;
    }

    public void setMetricsMeasurements(final List<MetricsMeasurement> metricsMeasurements) {
        this.metricsMeasurements = metricsMeasurements;
    }

    public Map<Metric, Double> getProjectSummary() {
        return projectSummary;
    }

    public void setProjectSummary(final Map<Metric, Double> projectSummary) {
        this.projectSummary = projectSummary;
    }

    public void addProjectSummaryEntry(final Metric metric, final Double value) {
        this.projectSummary.put(metric, value);
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(final String origin) {
        this.origin = origin;
    }
}
