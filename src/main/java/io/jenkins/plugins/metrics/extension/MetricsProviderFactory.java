package io.jenkins.plugins.metrics.extension;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import hudson.ExtensionPoint;
import hudson.model.Run;

import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.util.JenkinsFacade;

/**
 * Extension point to support custom metrics.
 */
public abstract class MetricsProviderFactory implements ExtensionPoint {
    /**
     * Returns all {@link MetricsProvider}s for the specified build, iterating over all registered
     * {@link MetricsProviderFactory}s.
     *
     * @param build
     *         the build to get the metrics for
     *
     * @return a list of {@link MetricsProvider}s
     */
    public static List<MetricsProvider> findAllFor(final Run<?, ?> build) {
        return findFactories().stream()
                .map(f -> f.getMetricsProviderFor(build))
                .toList();
    }

    /**
     * Returns all {@link MetricDefinition}s for the specified build, iterating over all registered
     * {@link MetricsProviderFactory}s.
     *
     * @param build
     *         the build to get the metrics for
     *
     * @return a list of {@link MetricsProvider}s, ordered by their priorities
     */
    public static List<MetricDefinition> findAllAvailableMetricsFor(final Run<?, ?> build) {
        return findFactories().stream()
                .map(f -> f.getAvailableMetricsFor(build))
                .flatMap(Collection::stream)
                .sorted()
                .toList();
    }

    private static List<MetricsProviderFactory> findFactories() {
        return new JenkinsFacade().getExtensionsFor(MetricsProviderFactory.class);
    }

    /**
     * Returns the {@link MetricsProvider} for the specified build. An implementing extension is supposed to calculate
     * the available metrics from the build on the fly.
     *
     * @param build
     *         the build to get the metrics for
     *
     * @return the {@link MetricsProvider} providing the actions
     */
    protected abstract MetricsProvider getMetricsProviderFor(Run<?, ?> build);

    /**
     * Returns all metrics this {@link MetricsProviderFactory} reports, for the specified build.
     *
     * @param build
     *         the build to get the metrics for
     *
     * @return containing all possibly reported metrics
     */
    public abstract Set<MetricDefinition> getAvailableMetricsFor(Run<?, ?> build);
}
