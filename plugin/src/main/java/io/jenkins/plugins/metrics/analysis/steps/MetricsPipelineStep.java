package io.jenkins.plugins.metrics.analysis.steps;

import java.io.IOException;
import java.io.Serializable;
import java.util.Set;

import org.eclipse.collections.impl.factory.Sets;

import edu.umd.cs.findbugs.annotations.NonNull;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.jenkinsci.Symbol;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Pipeline step that reports metrics from the build.
 */
public class MetricsPipelineStep extends Step implements Serializable {
    private static final long serialVersionUID = 1L;
    private String filePattern;

    /**
     * Creates a new instance of {@link MetricsPipelineStep}.
     */
    @DataBoundConstructor
    public MetricsPipelineStep() {
        super();
        // empty constructor required for Stapler
    }

    @Override
    public StepExecution start(final StepContext context) {
        return new Execution(context);
    }

    @DataBoundSetter
    public void setFilePattern(final String filePattern) {
        this.filePattern = filePattern;
    }

    public String getFilePattern() {
        return filePattern;
    }

    /**
     * Actually performs the execution of the associated step.
     */
    static class Execution extends SynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = -2840020502160375407L;

        Execution(@NonNull final StepContext context) {
            super(context);
        }

        @Override
        protected Void run() throws IOException, InterruptedException {
            FilePath workspace = getContext().get(FilePath.class);
            if (workspace == null) {
                throw new IOException("No workspace available for " + toString());
            }
            workspace.mkdirs();

            Run<?, ?> run = getContext().get(Run.class);
            if (run == null) {
                throw new IOException("Can't resolve Run for " + toString());
            }

            TaskListener taskListener = getContext().get(TaskListener.class);
            if (taskListener == null) {
                taskListener = TaskListener.NULL;
            }

            MetricsRecorder recorder = new MetricsRecorder();
            recorder.perform(run, workspace, taskListener);
            return null;
        }

    }

    /**
     * Descriptor for this step: defines the context and the UI labels.
     */
    @Extension
    @Symbol("reportMetrics")
    @SuppressWarnings("unused") // most methods are used by the corresponding jelly view
    public static class Descriptor extends StepDescriptor {
        @Override
        public String getFunctionName() {
            return "reportMetrics";
        }

        @NonNull
        @Override
        public String getDisplayName() {
            return Messages.Step_Name();
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Sets.immutable.of(FilePath.class, FlowNode.class, Run.class, TaskListener.class).castToSet();
        }
    }
}
