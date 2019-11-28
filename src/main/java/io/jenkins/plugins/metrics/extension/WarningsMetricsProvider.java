package io.jenkins.plugins.metrics.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.factory.Sets;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.MetricsProvider;

@Extension
@SuppressWarnings("unused") // used via the extension
public class WarningsMetricsProvider extends MetricsProviderFactory<ResultAction> {

    private static final Metric ERRORS = new Metric("ERRORS", "Errors",
            "An error, e.g. a compile error.", "warnings-ng-plugin");
    private static final Metric WARNINGS_HIGH = new Metric("WARNING_HIGH", "Warning (high)",
            "A warning with priority high.", "warnings-ng-plugin");
    private static final Metric WARNINGS_NORMAL = new Metric("WARNING_NORMAL", "Warning (normal)",
            "A warning with priority normal.", "warnings-ng-plugin");
    private static final Metric WARNINGS_LOW = new Metric("WARNING_LOW", "Warning (low)",
            "A warning with priority low.", "warnings-ng-plugin");

    private static final Metric AUTHORS = new Metric("AUTHORS", "Authors",
            "The number of authors for this file", "forensics-api-plugin");
    private static final Metric COMMITS = new Metric("COMMITS", "Commits",
            "The number of commits for this file", "warnings-ng-plugin");
    private static final Metric CREATED = new Metric("CREATED", "Created",
            "Time of file creation", "forensics-api-plugin");
    private static final Metric LASTMODIFIED = new Metric("LASTMODIFIED", "Last modified",
            "Time of last modification", "forensics-api-plugin");

    @Override
    public Class<ResultAction> type() {
        return ResultAction.class;
    }

    @Override
    public MetricsProvider getFor(final List<ResultAction> actions) {
        MetricsProvider provider = new MetricsProvider();
        provider.setOrigin("warnings-ng-plugin");

        RepositoryStatistics stats = actions.stream()
                .map(ResultAction::getResult)
                .map(AnalysisResult::getForensics)
                .reduce(new RepositoryStatistics(), (acc, r) -> {
                    acc.addAll(r);
                    return acc;
                });

        List<MetricsMeasurement> metricsMeasurements = actions.stream()
                .map(ResultAction::getResult)
                .map(AnalysisResult::getIssues)
                .peek(report -> {
                    provider.addProjectSummaryEntry(ERRORS, (double) report.getSizeOf(Severity.ERROR));
                    provider.addProjectSummaryEntry(WARNINGS_HIGH, (double) report.getSizeOf(Severity.WARNING_HIGH));
                    provider.addProjectSummaryEntry(WARNINGS_NORMAL,
                            (double) report.getSizeOf(Severity.WARNING_NORMAL));
                    provider.addProjectSummaryEntry(WARNINGS_LOW, (double) report.getSizeOf(Severity.WARNING_LOW));
                })
                .map(report -> report.groupByProperty("fileName"))
                .reduce(new HashMap<>(), (acc, map) -> {
                    map.forEach((key, report) -> acc.merge(key, report, Report::addAll));
                    return acc;
                })
                .entrySet().stream()
                .map(entry -> {
                    ClassMetricsMeasurement measurement = new ClassMetricsMeasurement();
                    Report report = entry.getValue();
                    measurement.setFileName(entry.getKey());

                    Issue first = report.get(0);
                    measurement.setPackageName(first.getPackageName());
                    measurement.setClassName(first.getBaseName().replace(".java", ""));

                    measurement.addMetric(ERRORS, report.getSizeOf(Severity.ERROR));
                    measurement.addMetric(WARNINGS_HIGH, report.getSizeOf(Severity.WARNING_HIGH));
                    measurement.addMetric(WARNINGS_NORMAL, report.getSizeOf(Severity.WARNING_NORMAL));
                    measurement.addMetric(WARNINGS_LOW, report.getSizeOf(Severity.WARNING_LOW));

                    FileStatistics fileStatistics = stats.get(entry.getKey());
                    measurement.addMetric(AUTHORS, fileStatistics.getNumberOfAuthors());
                    measurement.addMetric(COMMITS, fileStatistics.getNumberOfCommits());
                    // TODO change to absolute values once implemented upstream
                    measurement.addMetric(CREATED, fileStatistics.getAgeInDays());
                    measurement.addMetric(LASTMODIFIED, fileStatistics.getLastModifiedInDays());

                    return measurement;
                })
                .collect(Collectors.toList());

        provider.setMetricsMeasurements(metricsMeasurements);
        return provider;
    }

    @Override
    public Set<Metric> supportedMetrics() {
        return Sets.mutable.of(ERRORS, WARNINGS_HIGH, WARNINGS_NORMAL, WARNINGS_LOW, AUTHORS, COMMITS, CREATED,
                LASTMODIFIED);
    }
}
