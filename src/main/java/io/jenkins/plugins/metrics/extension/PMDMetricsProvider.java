package io.jenkins.plugins.metrics.extension;

import java.util.List;
import java.util.stream.Collectors;

import hudson.Extension;

import io.jenkins.plugins.metrics.analysis.MetricsAction;
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
}
