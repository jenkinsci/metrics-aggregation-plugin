package io.jenkins.plugins.metrics.model;

import java.io.IOException;

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
public class JobAction implements Action {
    private final Job<?, ?> owner;

    /**
     * Creates a new instance of {@link JobAction}.
     *
     * @param owner
     *         the job that owns this action
     */
    public JobAction(final Job<?, ?> owner) {
        this.owner = owner;
    }

    @Override
    public String getDisplayName() {
        return MetricsAction.NAME;
    }

    /**
     * Returns the icon URL for the side-panel in the job screen.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
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
}
