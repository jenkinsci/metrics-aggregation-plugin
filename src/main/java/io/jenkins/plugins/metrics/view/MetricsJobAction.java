package io.jenkins.plugins.metrics.view;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;
import hudson.Extension;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import jenkins.model.TransientActionFactory;

/**
 * A job action, displaying a link on the side panel of a job.
 *
 * @author Andreas Pabst
 */
@SuppressWarnings("unused")
public class MetricsJobAction implements Action {
    static final String METRICS_ICON = "symbol-solid/scale-unbalanced plugin-font-awesome-api";

    private final Job<?, ?> owner;

    /**
     * Creates a new instance of {@link MetricsJobAction}.
     *
     * @param owner
     *         the job that owns this action
     */
    public MetricsJobAction(final Job<?, ?> owner) {
        this.owner = owner;
    }

    @Override
    public String getDisplayName() {
        return MetricsViewAction.NAME;
    }

    /**
     * Returns the icon URL for the side-panel in the job screen.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    public String getIconFileName() {
        return METRICS_ICON;
    }

    @Override
    public String getUrlName() {
        return MetricsViewAction.ID;
    }

    /**
     * Redirects the index page to the last result.
     *
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @throws IOException
     *         in case of an error
     */
    @SuppressWarnings("unused") // Called by jelly view
    public void doIndex(final StaplerRequest2 request, final StaplerResponse2 response) throws IOException {
        final Run<?, ?> lastCompletedBuild = owner.getLastCompletedBuild();

        if (lastCompletedBuild != null) {
            response.sendRedirect2("../%d/%s".formatted(lastCompletedBuild.getNumber(), MetricsViewAction.ID));
        }
    }

    /**
     * Provides the metrics action for a job, i.e. the link in the side panel.
     */
    @Extension
    public static class JobActionFactory extends TransientActionFactory<Job<?, ?>> {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Class<Job<?, ?>> type() {
            return (Class) Job.class;
        }

        @NonNull
        @Override
        public Collection<? extends Action> createFor(@NonNull final Job<?, ?> target) {
            // TODO: Do we need to hide the action if there are no results?
            if (target.getFirstBuild() != null) {
                return Set.of(new MetricsJobAction(target));
            }
            return Collections.emptySet();
        }
    }
}
