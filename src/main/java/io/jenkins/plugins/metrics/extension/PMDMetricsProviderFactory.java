package io.jenkins.plugins.metrics.extension;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import hudson.Extension;

import io.jenkins.plugins.metrics.analysis.MetricsAction;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.MetricsProvider;

@Extension
public class PMDMetricsProviderFactory extends MetricsProviderFactory<MetricsAction> {

    @Override
    public Class<MetricsAction> type() {
        return MetricsAction.class;
    }

    @Override
    public MetricsProvider getFor(final List<MetricsAction> actions) {
        MetricsProvider provider = new MetricsProvider();
        provider.setOrigin("metrics-analysis-plugin (pmd)");

        provider.setMetricsMeasurements(actions.stream()
                .map(MetricsAction::getMetricsMeasurements)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );

        long numberOfClasses = actions.stream()
                .flatMap(m -> m.getMetricsMeasurements().stream())
                .filter(metricsMeasurement -> metricsMeasurement instanceof ClassMetricsMeasurement)
                .count();
        provider.addProjectSummaryEntry(String.format("%d classes", numberOfClasses));

        long totalLOC = actions.stream()
                .flatMap(m -> m.getMetricsMeasurements().stream())
                .filter(metricsMeasurement -> metricsMeasurement instanceof ClassMetricsMeasurement)
                .map(m -> m.getMetric("LOC").orElse(0.0))
                .reduce(0.0, Double::sum)
                .longValue();
        provider.addProjectSummaryEntry(String.format("%d lines of code", totalLOC));

        return provider;
    }

    @Override
    public LinkedHashSet<Metric> supportedMetricsFor(final List<MetricsAction> actions) {
        if (actions.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return new LinkedHashSet<>(Arrays.asList(
                new Metric("ATFD", "Access to foreign data", "", "metrics-analysis-plugin (pmd)"),
                new Metric("CLASS_FAN_OUT", "Fan out", "", "metrics-analysis-plugin (pmd)"),
                new Metric("LOC", "Lines of Code", "", "metrics-analysis-plugin (pmd)"),
                new Metric("NCSS", "Non-comment", "", "metrics-analysis-plugin (pmd)"),
                new Metric("NOAM", "Accessor methods", "", "metrics-analysis-plugin (pmd)"),
                new Metric("NOPA", "Public Attributes", "", "metrics-analysis-plugin (pmd)"),
                new Metric("TCC", "Class cohesion", "", "metrics-analysis-plugin (pmd)"),
                new Metric("WMC", "W. method count", "", "metrics-analysis-plugin (pmd)"),
                new Metric("WOC", "Weight of class", "", "metrics-analysis-plugin (pmd)")
        ));
    }
}
