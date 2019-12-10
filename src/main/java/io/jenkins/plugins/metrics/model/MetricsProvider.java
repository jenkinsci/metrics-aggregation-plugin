package io.jenkins.plugins.metrics.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MetricsProvider {

    private String origin;
    private List<MetricsMeasurement> metricsMeasurements = new ArrayList<>();
    private List<String> projectSummaryEntries = new LinkedList<>();

    public List<MetricsMeasurement> getMetricsMeasurements() {
        return metricsMeasurements;
    }

    public void setMetricsMeasurements(final List<MetricsMeasurement> metricsMeasurements) {
        this.metricsMeasurements = metricsMeasurements;
    }

    public List<String> getProjectSummaryEntries() {
        return projectSummaryEntries;
    }

    public void setProjectSummaryEntries(final List<String> projectSummaryEntries) {
        this.projectSummaryEntries = projectSummaryEntries;
    }

    public void addProjectSummaryEntry(final String entry) {
        this.projectSummaryEntries.add(entry);
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(final String origin) {
        this.origin = origin;
    }
}
