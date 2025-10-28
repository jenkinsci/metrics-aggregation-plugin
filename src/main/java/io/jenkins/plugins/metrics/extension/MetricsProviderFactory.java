package io.jenkins.plugins.metrics.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import hudson.ExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Action;
import jenkins.model.Jenkins;

import io.jenkins.plugins.metrics.model.MetricsProvider;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;

/**
 * Extension point to support custom metrics.
 *
 * @param <T> the type of the action this class supports
 */
public abstract class MetricsProviderFactory<T extends Action> implements ExtensionPoint {

    /**
     * Get all registered {@link MetricsProviderFactory}s.
     *
     * @return an {@link ExtensionList} containing all registered {@link MetricsProviderFactory}s
     */
    public static ExtensionList<MetricsProviderFactory> all() {
        return Jenkins.get().getExtensionList(MetricsProviderFactory.class);
    }

    /**
     * Get all {@link MetricsProvider}s for this actions, using all registered {@link MetricsProviderFactory}s.
     *
     * @param actions
     *         the actions of a run to use for getting the metrics
     *
     * @return a list of {@link MetricsProvider}s
     */
    @SuppressWarnings("unchecked")
    public static List<MetricsProvider> getAllFor(final List<? extends Action> actions) {
        return all().stream()
                .map(metricsProviderFactory -> {
                    List<? extends Action> actionsOfType = actions.stream()
                            .filter(action -> action.getClass().isAssignableFrom(metricsProviderFactory.type()))
                            .collect(Collectors.toList());

                    return metricsProviderFactory.getFor(actionsOfType);
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all {@link MetricsProvider}s for this actions, using all registered {@link MetricsProviderFactory}s.
     *
     * @param actions
     *         the actions of a run to use for getting the metrics
     *
     * @return a list of {@link MetricsProvider}s, ordered by their priorities
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<MetricDefinition> getAllSupportedMetricsFor(final List<? extends Action> actions) {
        ArrayList<MetricDefinition> supportedMetrics = all().stream()
                .map(metricsProviderFactory -> {
                    List<? extends Action> actionsOfType = actions.stream()
                            .filter(action -> action.getClass().isAssignableFrom(metricsProviderFactory.type()))
                            .collect(Collectors.toList());

                    return metricsProviderFactory.supportedMetricsFor(actionsOfType);
                })
                .reduce(new ArrayList<MetricDefinition>(), (acc, metrics) -> {
                    acc.addAll(metrics);
                    return acc;
                });

        // sort by priorities
        supportedMetrics.sort((m1, m2) -> {
            if (m1 == null || m2 == null) {
                throw new NullPointerException("NullPointerException while comparing " + m1 + " and " + m2);
            }

            return Integer.compare(m1.getPriority(), m2.getPriority());
        });

        return supportedMetrics;
    }

    /**
     * Get the type of action this {@link MetricsProviderFactory} is for. (This is necessary to be able to provide the
     * correct classes for the getFor(List) method.)
     *
     * @return the class of the action (same as the generic type)
     */
    public abstract Class<T> type();

    /**
     * Get all {@link MetricsMeasurement}s for the actions of a build. An implementing extension is supposed to filter
     * the relevant actions from the list and calculate their metrics from them.
     *
     * @param actions
     *         the actions of a build
     *
     * @return the {@link MetricsProvider} providing the actions
     */
    public abstract MetricsProvider getFor(List<T> actions);

    /**
     * Returns all metrics this {@link MetricsProviderFactory} reports, for a list of certain builds.
     *
     * @param actions
     *          the actions of a build
     *
     * @return a set containing all possibly reported metrics
     */
    public abstract ArrayList<MetricDefinition> supportedMetricsFor(List<T> actions);
}
