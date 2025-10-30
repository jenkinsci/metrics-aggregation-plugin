package io.jenkins.plugins.metrics.extension;

import org.junit.jupiter.api.Test;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.IssueBuilder;
import edu.hm.hafner.analysis.Report;

import java.util.Collections;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.metrics.model.MetricsProviderAssert;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WarningsMetricsProviderFactoryTest {

    /**
     * Get measurements with forensics missing.
     */
    @Test
    @org.jvnet.hudson.test.Issue("JENKINS-61401")
    void getMetricsWithMissingForensics() {
        WarningsMetricsProviderFactory warningsFactory = new WarningsMetricsProviderFactory();

        AnalysisResult analysisResult = mock(AnalysisResult.class);
        when(analysisResult.getForensics()).thenReturn(new RepositoryStatistics());

        Report report = new Report();

        Issue issue = new IssueBuilder().setFileName("Test.java").build();
        report.add(issue);
        when(analysisResult.getIssues()).thenReturn(report);

        ResultAction action = mock(ResultAction.class);
        when(action.getResult()).thenReturn(analysisResult);

        MetricsProvider metricsProvider = warningsFactory.getFor(Collections.singletonList(action));

        assertThat(metricsProvider.getMetricsMeasurements()).hasSize(1);
        MetricsProviderAssert.assertThat(metricsProvider).hasProjectSummaryEntries("0 Errors",
                "1 Warnings (0 high, 1 normal, 0 low)");
    }
}
