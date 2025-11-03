package io.jenkins.plugins.metrics.extension;

import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;

import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

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
    void getMetricsWithMissingForensics() {
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
}
