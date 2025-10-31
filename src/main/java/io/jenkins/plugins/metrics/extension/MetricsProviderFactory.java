package io.jenkins.plugins.metrics.extension;

import java.util.Collection;
import java.util.List;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Run;
import jenkins.model.Jenkins;

import io.jenkins.plugins.metrics.model.metric.MetricDefinition;

/**
 * Extension point to support custom metrics.
 */
public abstract class MetricsProviderFactory implements ExtensionPoint {
    /**
     * Get all registered {@link MetricsProviderFactory}s.
     *
     * @return an {@link ExtensionList} containing all registered {@link MetricsProviderFactory}s
     */
    public static ExtensionList<MetricsProviderFactory> all() {
        return Jenkins.get().getExtensionList(MetricsProviderFactory.class);
    }

    /**
     * Returns all {@link MetricsProvider}s for the specified build, iterating over all registered
     * {@link MetricsProviderFactory}s.
     *
     * @param build
     *         the build to get the metrics for
     *
     * @return a list of {@link MetricsProvider}s
     */
    public static List<MetricsProvider> getAllFor(final Run<?, ?> build) {
        return all().stream()
                .map(f -> f.getFor(build))
                .toList();
    }

    /**
     * Get all {@link MetricsProvider}s for the specified build actions, using all registered
     * {@link MetricsProviderFactory}s.
     *
     * @param build
     *         the build to get the metrics for
     *
     * @return a list of {@link MetricsProvider}s, ordered by their priorities
     */
    public static List<MetricDefinition> getAllSupportedMetricsFor(final Run<?, ?> build) {
        return all().stream()
                .map(f -> f.supportedMetricsFor(build))
                .flatMap(Collection::stream)
                .sorted()
                .toList();
    }

    /**
     * Returns the {@link MetricsProvider} for the specified build. An implementing extension is supposed calculate
     * their metrics from the build on the fly.
     *
     * @param build
     *         the build to get the metrics for
     *
     * @return the {@link MetricsProvider} providing the actions
     */
    public abstract MetricsProvider getFor(Run<?, ?> build);

    /**
     * Returns all metrics this {@link MetricsProviderFactory} reports, for the specified build.
     *
     * @param build
     *         the build to get the metrics for
     *
     * @return containing all possibly reported metrics
     */
    public abstract List<MetricDefinition> supportedMetricsFor(Run<?, ?> build);
}
