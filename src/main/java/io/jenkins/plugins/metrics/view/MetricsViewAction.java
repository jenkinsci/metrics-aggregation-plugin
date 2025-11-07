package io.jenkins.plugins.metrics.view;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Collection;
import java.util.Set;

import org.kohsuke.stapler.StaplerProxy;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.model.TransientActionFactory;

import static io.jenkins.plugins.metrics.view.MetricsJobAction.*;

/**
 * Action for displaying a metric on the side of a job.
 */
public class MetricsViewAction implements RunAction2, StaplerProxy {
    private transient Run<?, ?> owner;

    /**
     * The id for the plugin.
     */
    public static final String ID = "metrics-aggregation";
    /**
     * The name of the plugin.
     */
    public static final String NAME = Messages.metrics();

    /**
     * Create a new {@link MetricsViewAction}.
     *
     * @param owner
     *         the owning {@link Run}
     */
    public MetricsViewAction(final Run<?, ?> owner) {
        this.owner = owner;
    }

    @NonNull
    @Override
    public String getIconFileName() {
        return METRICS_ICON;
    }

    @NonNull
    @Override
    public String getDisplayName() {
        return NAME;
    }

    @NonNull
    @Override
    public String getUrlName() {
        return ID;
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
     * Returns the detail view for metrics for all Stapler requests.
     *
     * @return the detail view for metrics
     */
    @Override
    public Object getTarget() {
        return new MetricsView(owner);
    }

    /**
     * {@link TransientActionFactory} providing {@link MetricsViewAction}s.
     */
    @Extension
    public static class ViewActionFactory extends TransientActionFactory<Run> {
        @SuppressWarnings("rawtypes")
        @Override
        public Class<Run> type() {
            return Run.class;
        }

        @NonNull
        @Override
        public Collection<? extends Action> createFor(@NonNull final Run target) {
            return Set.of(new MetricsViewAction(target));
        }
    }
}
