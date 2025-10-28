package io.jenkins.plugins.metrics.view;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

import org.kohsuke.stapler.StaplerProxy;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.model.TransientActionFactory;

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
     * The icon to use with links.
     */
    public static final String ICON = "/plugin/metrics-aggregation/icons/metrics-24x24.png";

    /**
     * Create a new {@link MetricsViewAction}.
     *
     * @param owner
     *         the owning {@link Run}
     */
    public MetricsViewAction(final Run<?, ?> owner) {
        this.owner = owner;
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

        @Override
        public Class<Run> type() {
            return Run.class;
        }

        @Nonnull
        @Override
        public Collection<? extends Action> createFor(@Nonnull final Run target) {
            return Collections.singleton(new MetricsViewAction(target));
        }
    }
}
