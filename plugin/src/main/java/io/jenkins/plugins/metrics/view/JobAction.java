package io.jenkins.plugins.metrics.view;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nonnull;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
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
        return ViewAction.NAME;
    }

    /**
     * Returns the icon URL for the side-panel in the job screen.
     *
     * @return the icon URL for the side-panel in the job screen
     */
    @Override
    public String getIconFileName() {
        return ViewAction.ICON;
    }

    @Override
    public String getUrlName() {
        return ViewAction.ID;
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
            response.sendRedirect2(String.format("../%d/%s", lastCompletedBuild.getNumber(), ViewAction.ID));
        }
    }

    @Extension
    public static class JobActionFactory extends TransientActionFactory<Job> {

        @Override
        public Class<Job> type() {
            return Job.class;
        }

        @Nonnull
        @Override
        public Collection<? extends Action> createFor(@Nonnull final Job target) {
            if (target.getFirstBuild() != null) {
                return Collections.singleton(new JobAction(target));
            }
            return Collections.emptySet();
        }
    }

}
