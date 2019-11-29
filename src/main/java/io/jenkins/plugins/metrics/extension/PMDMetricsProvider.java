package io.jenkins.plugins.metrics.extension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.factory.Sets;

import hudson.Extension;

import io.jenkins.plugins.metrics.analysis.MetricsAction;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricsProvider;

@Extension
public class PMDMetricsProvider extends MetricsProviderFactory<MetricsAction> {

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

        return provider;
    }

    @Override
    public Set<Metric> supportedMetrics() {
        return Sets.mutable.of(
                new Metric("ATFD", "Access to foreign data", "", "metrics-analysis-plugin (pmd)"),
                new Metric("CLASS_FAN_OUT", "Fan out", "", "metrics-analysis-plugin (pmd)"),
                new Metric("LOC", "Lines of Code", "", "metrics-analysis-plugin (pmd)"),
                new Metric("NCSS", "Non-comment", "", "metrics-analysis-plugin (pmd)"),
                new Metric("NOAM", "Accessor methods", "", "metrics-analysis-plugin (pmd)"),
                new Metric("NOPA", "Public Attributes", "", "metrics-analysis-plugin (pmd)"),
                new Metric("TCC", "Class cohesion", "", "metrics-analysis-plugin (pmd)"),
                new Metric("WMC", "W. method count", "", "metrics-analysis-plugin (pmd)"),
                new Metric("WOC", "Weight of class", "", "metrics-analysis-plugin (pmd)")
        );
    }
}
