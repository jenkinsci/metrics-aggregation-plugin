package io.jenkins.plugins.metrics.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.forensics.miner.FileStatistics;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.metrics.model.MetricsProvider;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

@Extension
@SuppressWarnings("unused") // used via the extension
public class WarningsMetricsProviderFactory extends MetricsProviderFactory<ResultAction> {

    private static final MetricDefinition ERRORS = new MetricDefinition("ERRORS",
            "Errors",
            "An error, e.g. a compile error.",
            "warnings-ng-plugin",
            10,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition WARNINGS_HIGH = new MetricDefinition("WARNING_HIGH",
            "Warning (high)",
            "A warning with priority high.",
            "warnings-ng-plugin",
            10,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition WARNINGS_NORMAL = new MetricDefinition("WARNING_NORMAL",
            "Warning (normal)",
            "A warning with priority normal.",
            "warnings-ng-plugin",
            10,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition WARNINGS_LOW = new MetricDefinition("WARNING_LOW",
            "Warning (low)",
            "A warning with priority low.",
            "warnings-ng-plugin",
            10,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition AUTHORS = new MetricDefinition("AUTHORS",
            "Authors",
            "The number of unique authors for this file.",
            "forensics-api-plugin",
            20,
            ArrayUtils.toArray(Scope.CLASS));
    private static final MetricDefinition COMMITS = new MetricDefinition("COMMITS",
            "Commits",
            "The number of commits for this file.",
            "forensics-api-plugin",
            20,
            ArrayUtils.toArray(Scope.CLASS));

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

        Report allIssues = actions.stream()
                .map(ResultAction::getResult)
                .map(AnalysisResult::getIssues)
                .reduce(new Report(), Report::addAll);

        provider.addProjectSummaryEntry(String.format("%d Errors", allIssues.getSizeOf(Severity.ERROR)));
        provider.addProjectSummaryEntry(String.format("%d Warnings (%d high, %d normal, %d low)",
                allIssues.getSizeOf(Severity.WARNING_HIGH)
                        + allIssues.getSizeOf(Severity.WARNING_NORMAL)
                        + allIssues.getSizeOf(Severity.WARNING_LOW),
                allIssues.getSizeOf(Severity.WARNING_HIGH),
                allIssues.getSizeOf(Severity.WARNING_NORMAL),
                allIssues.getSizeOf(Severity.WARNING_LOW)));

        List<MetricsMeasurement> metricsMeasurements = allIssues
                .groupByProperty("fileName")
                .entrySet().stream()
                .map(entry -> {
                    ClassMetricsMeasurement measurement = new ClassMetricsMeasurement();
                    Report report = entry.getValue();
                    measurement.setFileName(entry.getKey());

                    Issue first = report.get(0);
                    measurement.setPackageName(first.getPackageName());
                    measurement.setClassName(first.getBaseName().replace(".java", ""));

                    measurement.addMetric(new IntegerMetric(ERRORS, report.getSizeOf(Severity.ERROR)));
                    measurement.addMetric(new IntegerMetric(WARNINGS_HIGH, report.getSizeOf(Severity.WARNING_HIGH)));
                    measurement.addMetric(new IntegerMetric(WARNINGS_NORMAL,
                            report.getSizeOf(Severity.WARNING_NORMAL)));
                    measurement.addMetric(new IntegerMetric(WARNINGS_LOW, report.getSizeOf(Severity.WARNING_LOW)));

                    FileStatistics fileStatistics = stats.get(entry.getKey());
                    measurement.addMetric(new IntegerMetric(AUTHORS, fileStatistics.getNumberOfAuthors()));
                    measurement.addMetric(new IntegerMetric(COMMITS, fileStatistics.getNumberOfCommits()));

                    return measurement;
                })
                .collect(Collectors.toList());

        provider.setMetricsMeasurements(metricsMeasurements);
        return provider;
    }

    @Override
    public ArrayList<MetricDefinition> supportedMetricsFor(final List<ResultAction> actions) {
        if (actions.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(ERRORS,
                WARNINGS_HIGH,
                WARNINGS_NORMAL,
                WARNINGS_LOW,
                AUTHORS,
                COMMITS));
    }
}
