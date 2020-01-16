
package io.jenkins.plugins.metrics.analysis.steps;

import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;

import io.jenkins.plugins.metrics.analysis.MetricsAction;
import io.jenkins.plugins.metrics.analysis.MetricsActor;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;

/**
 * Report metrics (freestyle?).
 *
 * @author Andreas Pabst
 */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.ExcessiveClassLength", "PMD.ExcessiveImports", "PMD.TooManyFields", "PMD.DataClass", "ClassDataAbstractionCoupling", "ClassFanOutComplexity"})
public class MetricsRecorder extends Recorder implements SimpleBuildStep {

    private String filePattern;

    /**
     * Creates a new instance of {@link MetricsRecorder}.
     */
    @DataBoundConstructor
    public MetricsRecorder() {
        super();

        // empty constructor required for Stapler
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public Descriptor getDescriptor() {
        return (Descriptor) super.getDescriptor();
    }

    @DataBoundSetter
    public void setFilePattern(final String filePattern) {
        this.filePattern = filePattern;
    }

    public String getFilePattern() {
        return filePattern;
    }

    @Override
    public boolean perform(final AbstractBuild<?, ?> build, final Launcher launcher, final BuildListener listener)
            throws InterruptedException, IOException {
        FilePath workspace = build.getWorkspace();
        if (workspace == null) {
            throw new IOException("No workspace found for " + build);
        }

        perform(build, workspace, listener);

        return true;
    }

    void perform(final Run<?, ?> run, final FilePath workspace, final TaskListener listener)
            throws InterruptedException, IOException {

        PrintStream log = listener.getLogger();

        log.println("[Metrics] Start collecting metrics");

        List<MetricsMeasurement> metricsReport = workspace.act(new MetricsActor(filePattern, listener));

        // merge all the measurements together
        List<MetricsMeasurement> metricsMeasurements = metricsReport.stream()
                .collect(Collectors.groupingBy(MetricsMeasurement::getQualifiedClassName))
                .values().stream()
                .map(perFileMeasurements -> perFileMeasurements.stream().reduce(MetricsMeasurement::merge)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        run.addAction(new MetricsAction(metricsMeasurements));

        log.println("[Metrics] Finished collecting metrics");
    }

    @Override
    public void perform(final Run<?, ?> run, final FilePath workspace, final Launcher launcher,
            final TaskListener taskListener) throws InterruptedException, IOException {

        perform(run, workspace, taskListener);
    }

    /**
     * Descriptor for this step: defines the context and the UI elements.
     */
    @Extension
    @SuppressWarnings("unused") // most methods are used by the corresponding jelly view
    public static class Descriptor extends BuildStepDescriptor<Publisher> {

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Step_Name();
        }

        @Override
        public boolean isApplicable(final Class<? extends AbstractProject> jobType) {
            return true;
        }
    }
}
