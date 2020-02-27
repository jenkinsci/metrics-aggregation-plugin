package io.jenkins.plugins.metrics.analysis;

import java.util.Collection;
import java.util.List;

import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;

/**
 * An action for recording metrics in a jenkins build.
 */
public class MetricsAction implements RunAction2, LastBuildAction {
    private final List<MetricsMeasurement> metricsMeasurements;

    /**
     * Create a new {@link MetricsAction}.
     *
     * @param metricsMeasurements
     *         the {@link MetricsMeasurement}s for this action
     */
    public MetricsAction(final List<MetricsMeasurement> metricsMeasurements) {
        this.metricsMeasurements = metricsMeasurements;
    }

    @Nullable
    @Override
    public String getIconFileName() {
        return null;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return null;
    }

    @Nullable
    @Override
    public String getUrlName() {
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        // This action is just for recording metrics in a build. But metrics can also be retrieved from other actions, 
        // so showing the action at the project is done via a separate class, MetricsJobAction.
        return null;
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
    }

    public final List<MetricsMeasurement> getMetricsMeasurements() {
        return metricsMeasurements;
    }
}
