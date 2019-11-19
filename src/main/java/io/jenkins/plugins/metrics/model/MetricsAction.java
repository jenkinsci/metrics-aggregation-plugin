package io.jenkins.plugins.metrics.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.StaplerProxy;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep.LastBuildAction;

public class MetricsAction implements RunAction2, LastBuildAction, StaplerProxy {
    private transient Run<?, ?> owner;
    private final List<MetricsMeasurement> metricsMeasurements;

    public static final String ID = "metrics";
    public static final String NAME = "Metrics";
    public static final String ICON = "/plugin/warnings-ng/icons/metrics-24x24.png";

    public MetricsAction(final MetricsReport metricsReport) {
        this.metricsMeasurements = metricsReport;
    }

    @Nullable
    @Override
    public String getIconFileName() {
        return ICON;
    }

    @Nullable
    @Override
    public String getDisplayName() {
        return NAME;
    }

    @Nullable
    @Override
    public String getUrlName() {
        return ID;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        return Collections.singleton(new MetricsJobAction(owner.getParent()));
    }

    @Override
    public void onAttached(final Run<?, ?> r) {
        owner = r;
    }

    @Override
    public void onLoad(final Run<?, ?> r) {
        owner = r;
    }

    /**
     * Returns the detail view for issues for all Stapler requests.
     *
     * @return the detail view for issues
     */
    @Override
    public Object getTarget() {
        return new MetricsDetail(owner, metricsMeasurements);
    }

    public List<MetricsMeasurement> getMetricsMeasurements() {
        return this.metricsMeasurements;
    }
}
