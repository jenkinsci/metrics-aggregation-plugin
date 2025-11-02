package io.jenkins.plugins.metrics.extension;

import org.apache.commons.lang3.ArrayUtils;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import hudson.Extension;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

/**
 * {@link MetricsProviderFactory} for the number of warnings and git forensics data. Uses the warnings-ng-plugin and
 * forensics-api-plugin as sources.
 */
@Extension
@SuppressWarnings("unused") // used via the extension
// TODO: This class should only collect warnings and should be moved to the warnings plugin
public class WarningsMetricsProviderFactory extends MetricsProviderFactory {
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
    public MetricsProvider getFor(final Run<?, ?> build) {
        var provider = new MetricsProvider();
        provider.setOrigin("warnings-ng-plugin");

        var actions = build.getActions(ResultAction.class);
        var stats = actions.stream()
                .map(ResultAction::getResult)
                .map(AnalysisResult::getForensics)
                .reduce(new RepositoryStatistics(), (acc, r) -> {
                    acc.addAll(r);
                    return acc;
                });

        var allIssues = actions.stream()
                .map(ResultAction::getResult)
                .map(AnalysisResult::getIssues)
                .reduce(new Report(), Report::addAll);

        provider.addProjectSummaryEntry("%d Errors".formatted(allIssues.getSizeOf(Severity.ERROR)));
        provider.addProjectSummaryEntry("%d Warnings (%d high, %d normal, %d low)".formatted(
                allIssues.getSizeOf(Severity.WARNING_HIGH)
                        + allIssues.getSizeOf(Severity.WARNING_NORMAL)
                        + allIssues.getSizeOf(Severity.WARNING_LOW),
                allIssues.getSizeOf(Severity.WARNING_HIGH),
                allIssues.getSizeOf(Severity.WARNING_NORMAL),
                allIssues.getSizeOf(Severity.WARNING_LOW)));

        List<MetricsMeasurement> metricsMeasurements = allIssues
                .groupByProperty("fileName")
                .entrySet().stream()
                .map(entry -> createMetric(entry, stats))
                .collect(Collectors.toList());

        provider.setMetricsMeasurements(metricsMeasurements);
        return provider;
    }

    private ClassMetricsMeasurement createMetric(final Entry<String, Report> entry,
            final RepositoryStatistics stats) {
        var measurement = new ClassMetricsMeasurement();
        var report = entry.getValue();
        measurement.setFileName(entry.getKey());

        var first = report.get(0);
        measurement.setPackageName(first.getPackageName());
        measurement.setClassName(first.getBaseName().replace(".java", ""));

        measurement.addMetric(new IntegerMetric(ERRORS, report.getSizeOf(Severity.ERROR)));
        measurement.addMetric(new IntegerMetric(WARNINGS_HIGH, report.getSizeOf(Severity.WARNING_HIGH)));
        measurement.addMetric(new IntegerMetric(WARNINGS_NORMAL,
                report.getSizeOf(Severity.WARNING_NORMAL)));
        measurement.addMetric(new IntegerMetric(WARNINGS_LOW, report.getSizeOf(Severity.WARNING_LOW)));

        if (stats.contains(entry.getKey())) {
            var fileStatistics = stats.get(entry.getKey());
            measurement.addMetric(new IntegerMetric(AUTHORS, fileStatistics.getNumberOfAuthors()));
            measurement.addMetric(new IntegerMetric(COMMITS, fileStatistics.getNumberOfCommits()));
        }

        return measurement;
    }

    @Override
    public List<MetricDefinition> supportedMetricsFor(final Run<?, ?> build) {
        var actions = build.getActions(ResultAction.class);
        if (actions.isEmpty()) {
            return new ArrayList<>();
        }

        return List.of(ERRORS, WARNINGS_HIGH, WARNINGS_NORMAL, WARNINGS_LOW, AUTHORS, COMMITS);
    }
}
