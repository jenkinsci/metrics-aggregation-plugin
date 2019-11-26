package io.jenkins.plugins.metrics.analysis;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.Nullable;

import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.MetricsReport;
import io.jenkins.plugins.metrics.view.JobAction;

public class MetricsAction implements RunAction2, LastBuildAction {
    private transient Run<?, ?> owner;
    private final List<MetricsMeasurement> metricsMeasurements;

    public MetricsAction(final MetricsReport metricsReport) {
        this.metricsMeasurements = metricsReport;
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
        return Collections.singleton(new JobAction(owner.getParent()));
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        owner = r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        owner = r;
    }

    public final List<MetricsMeasurement> getMetricsMeasurements() {
        return metricsMeasurements;
    }
}
