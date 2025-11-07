package io.jenkins.plugins.metrics.extension;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import java.util.List;

import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;

import static io.jenkins.plugins.metrics.assertions.Assertions.*;
import static org.mockito.Mockito.*;

class WarningsMetricsProviderFactoryTest {
    /**
     * Get measurements with forensics missing.
     */
    @Test
    @Issue("JENKINS-61401")
    void shouldGetMetricsWithMissingForensics() {
        var warningsFactory = new WarningsMetricsProviderFactory();

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getForensics()).thenReturn(new RepositoryStatistics());

        var report = new Report();

        var issue = new IssueBuilder().setFileName("Test.java").build();
        report.add(issue);
        when(analysisResult.getIssues()).thenReturn(report);

        ResultAction action = mock(ResultAction.class);
        when(action.getResult()).thenReturn(analysisResult);

        var run = mock(Run.class);
        when(run.getActions(ResultAction.class)).thenReturn(List.of(action));

        var metricsProvider = warningsFactory.getMetricsProviderFor(run);

        assertThat(metricsProvider.getMetricsMeasurements()).hasSize(1);
        assertThat(metricsProvider).hasProjectSummaryEntries("0 Errors",
                "1 Warnings (0 high, 1 normal, 0 low)");
    }

    @Test
    void shouldCombineActions() {
        try (var builder = new IssueBuilder()) {
            var factory = new WarningsMetricsProviderFactory();

            var first = new Report();
            first.add(builder.setFileName("Test1.java").build());
            first.add(builder.setFileName("Test2.java").build());

            var second = new Report();
            second.add(builder.setFileName("Test3.java").setSeverity(Severity.WARNING_HIGH).build());

            var firstAction = createAction(first);
            var secondAction = createAction(second);

            var run = mock(Run.class);
            when(run.getActions(ResultAction.class)).thenReturn(List.of(firstAction, secondAction));

            var metricsProvider = factory.getMetricsProviderFor(run);

            assertThat(metricsProvider.getMetricsMeasurements()).hasSize(3);
            assertThat(metricsProvider)
                    .hasProjectSummaryEntries("0 Errors", "3 Warnings (1 high, 2 normal, 0 low)");
        }
    }

    private ResultAction createAction(final Report first) {
        ResultAction action = mock(ResultAction.class);
        AnalysisResult result = mock(AnalysisResult.class);
        when(result.getForensics()).thenReturn(new RepositoryStatistics());
        when(result.getIssues()).thenReturn(first);
        when(action.getResult()).thenReturn(result);
        return action;
    }
}
