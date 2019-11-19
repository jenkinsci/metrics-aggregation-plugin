package io.jenkins.plugins.metrics.model;

import java.io.IOException;

import edu.umd.cs.findbugs.annotations.Nullable;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;

/**
 * A job action displays a link on the side panel of a job.
 *
 * @author Andreas Pabst
 */
public class MetricsJobAction implements Action {
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

    /**
     * Returns the ID of this action and the ID of the associated results.
     *
     * @return the ID
     */
    public String getId() {
        return MetricsAction.ID;
    }

    @Override
    public String getDisplayName() {
        return MetricsAction.NAME;
    }

    /**
     * Returns the job this action belongs to.
     *
     * @return the job
     */
    public Job<?, ?> getOwner() {
        return owner;
    }

    /**
     * Returns the icon URL for the side-panel in the job screen. If there is no valid result yet, then {@code null} is
     * returned.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    @Nullable
    public String getIconFileName() {
        return MetricsAction.ICON;
    }

    @Override
    public String getUrlName() {
        return MetricsAction.ID;
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
    public void doIndex(final StaplerRequest request, final StaplerResponse response) throws IOException {
        final Run<?, ?> lastCompletedBuild = owner.getLastCompletedBuild();

        if (lastCompletedBuild != null) {
            response.sendRedirect2(String.format("../%d/%s", lastCompletedBuild.getNumber(), MetricsAction.ID));
        }
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}
