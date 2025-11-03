package io.jenkins.plugins.metrics.column;

import org.apache.commons.lang3.StringUtils;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.Optional;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;

import io.jenkins.plugins.metrics.extension.MetricsProviderFactory;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.view.MetricsViewAction;

/**
 * Column for displaying metrics in the project overview.
 *
 * @author Andreas Pabst
 */
public class ProjectMetricsColumn extends ListViewColumn {
    private String name = "Lines of Code";
    private String metricId = "LOC";

    /**
     * Create a new {@link ProjectMetricsColumn}.
     */
    @DataBoundConstructor
    public ProjectMetricsColumn() {
        super();
        // empty constructor required for stapler
    }

    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the column.
     *
     * @param name
     *         the name of the column
     */
    @DataBoundSetter
    public void setName(final String name) {
        this.name = name;
    }

    public String getMetricId() {
        return metricId;
    }

    /**
     * Defines which metric should be shown in the column.
     *
     * @param metricId
     *         the metric id to show
     */
    @DataBoundSetter
    public void setMetricId(final String metricId) {
        this.metricId = metricId;
    }

    /**
     * Returns the total number of issues for the selected static analysis tool in a given job.
     *
     * @param job
     *         the job to select
     *
     * @return the number of issues for a tool in a given job
     */
    @SuppressWarnings("unused") // called bv jelly view
    public Optional<String> getMetricValue(final Job<?, ?> job) {
        Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return Optional.empty();
        }

        return MetricsProviderFactory.findAllFor(lastCompletedBuild)
                .stream()
                .flatMap(metricsProvider -> metricsProvider.getProjectMetrics().stream())
                .filter(metric -> metric.getId().equals(metricId))
                .map(Metric::renderValue)
                .findFirst();
    }

    /**
     * Returns the URL to the metrics view of a run.
     *
     * @param job
     *         the job to select
     *
     * @return the URL to the metrics
     */
    @SuppressWarnings("unused") // called bv jelly view
    public String getUrl(final Job<?, ?> job) {
        Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
        if (lastCompletedBuild == null) {
            return StringUtils.EMPTY;
        }

        return lastCompletedBuild.getUrl() + MetricsViewAction.ID;
    }

    /**
     * Extension point registration.
     *
     * @author Andreas Pabst
     */
    @Extension(optional = true)
    public static class MetricsTableColumnDescriptor extends ListViewColumnDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.MetricsTableColumnDescriptor_Name();
        }

        /**
         * Do not show the column by default.
         *
         * @return false, to prevent showing this column by default
         */
        @Override
        public boolean shownByDefault() {
            return false;
        }
    }
}
