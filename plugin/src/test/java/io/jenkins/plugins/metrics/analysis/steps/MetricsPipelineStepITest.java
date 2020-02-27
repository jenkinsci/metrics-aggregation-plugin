package io.jenkins.plugins.metrics.analysis.steps;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import hudson.model.Run;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for the class {@link MetricsPipelineStep}.
 */
public class MetricsPipelineStepITest {

    /**
     * Jenkins rule for the integration test.
     */
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    /**
     * Test if the correct files are analyzed.
     *
     * @throws Exception
     *         test -> ignored
     */
    @Test
    public void shouldMatchPatternFromPipeline() throws Exception {
        WorkflowJob project = createProjectWithPattern("Test.java");
        Run<?, ?> build = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));

        assertThat(build.getLog(100)).contains("[Pipeline] reportMetrics",
                "[Metrics] Start collecting metrics",
                "[Metrics] Analyzing 1 files matching the pattern 'Test.java' in " + jenkins.getInstance()
                        .getWorkspaceFor(project));
    }

    /**
     * Test if the correct files are analyzed. Here the pattern should not match.
     *
     * @throws Exception
     *         test -> ignored
     */
    @Test
    public void shouldNotMatchPatternFromPipeline() throws Exception {
        WorkflowJob project = createProjectWithPattern("doesnotmatch");
        Run<?, ?> build = jenkins.assertBuildStatusSuccess(project.scheduleBuild2(0));

        assertThat(build.getLog(100)).contains("[Pipeline] reportMetrics",
                "[Metrics] Start collecting metrics",
                "[Metrics] Analyzing 0 files matching the pattern 'doesnotmatch' in " + jenkins.getInstance()
                        .getWorkspaceFor(project));
    }

    private WorkflowJob createProjectWithPattern(final String pattern) throws IOException {
        WorkflowJob project = jenkins.createProject(WorkflowJob.class);
        project.setDefinition(new CpsFlowDefinition("node {\n"
                + "    sh script: 'echo \"public class Test { }\" > Test.java'\n"
                + "    reportMetrics filePattern: '" + pattern + "'\n"
                + "}", true));
        return project;
    }
}
