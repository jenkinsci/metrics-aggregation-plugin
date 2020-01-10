package io.jenkins.plugins.metrics.view;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.StaplerProxy;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Run;
import jenkins.model.RunAction2;
import jenkins.model.TransientActionFactory;

public class ViewAction implements RunAction2, StaplerProxy {
    private transient Run<?, ?> owner;

    public static final String ID = "metrics";
    public static final String NAME = Messages.Metrics();
    public static final String ICON = "/plugin/metrics-analysis/icons/metrics-24x24.png";

    public ViewAction(final Run<?, ?> owner) {
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
        return new MetricsDetailView(owner);
    }

    @Extension
    public static class ViewActionFactory extends TransientActionFactory<Run> {

        @Override
        public Class<Run> type() {
            return Run.class;
        }

        @Nonnull
        @Override
        public Collection<? extends Action> createFor(@Nonnull final Run target) {
            return Collections.singleton(new ViewAction(target));
        }
    }
}
