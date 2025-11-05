package io.jenkins.plugins.metrics.extension;

import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import hudson.Extension;
import hudson.model.Run;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.forensics.miner.RepositoryStatistics;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement.ClassMetricsMeasurementBuilder;
import io.jenkins.plugins.metrics.model.IntegerMetric;
import io.jenkins.plugins.metrics.model.MetricDefinition;
import io.jenkins.plugins.metrics.model.MetricDefinition.MetricDefinitionBuilder;
import io.jenkins.plugins.metrics.model.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;

/**
 * {@link MetricsProviderFactory} for the number of warnings and git forensics data. Uses the warnings-ng-plugin and
 * forensics-api-plugin as sources.
 */
@Extension
@SuppressWarnings("unused") // used via the extension
// TODO: This class should only collect warnings and should be moved to the warnings plugin
public class WarningsMetricsProviderFactory extends MetricsProviderFactory {
    private static final MetricDefinition ERRORS = new MetricDefinitionBuilder("ERRORS")
            .withDisplayName("Errors")
            .withDescription("An error, e.g. a compile error.")
            .withReportedBy("warnings-ng-plugin")
            .withPriority(10)
            .withScopes(Scope.METHOD, Scope.CLASS)
            .build();
    private static final MetricDefinition WARNINGS_HIGH = new MetricDefinitionBuilder("WARNING_HIGH")
            .withDisplayName("Warning (high)")
            .withDescription("A warning with priority high.")
            .withReportedBy("warnings-ng-plugin")
            .withPriority(10)
            .withScopes(Scope.METHOD, Scope.CLASS)
            .build();
    private static final MetricDefinition WARNINGS_NORMAL = new MetricDefinitionBuilder("WARNING_NORMAL")
            .withDisplayName("Warning (normal)")
            .withDescription("A warning with priority normal.")
            .withReportedBy("warnings-ng-plugin")
            .withPriority(10)
            .withScopes(Scope.METHOD, Scope.CLASS)
            .build();
    private static final MetricDefinition WARNINGS_LOW = new MetricDefinitionBuilder("WARNING_LOW")
            .withDisplayName("Warning (low)")
            .withDescription("A warning with priority low.")
            .withReportedBy("warnings-ng-plugin")
            .withPriority(10)
            .withScopes(Scope.METHOD, Scope.CLASS)
            .build();
    private static final MetricDefinition AUTHORS = new MetricDefinitionBuilder("AUTHORS")
            .withDisplayName("Authors")
            .withDescription("The number of unique authors for this file.")
            .withReportedBy("forensics-api-plugin")
            .withPriority(20)
            .withScopes(Scope.CLASS)
            .build();
    private static final MetricDefinition COMMITS = new MetricDefinitionBuilder("COMMITS")
            .withDisplayName("Commits")
            .withDescription("The number of commits for this file.")
            .withReportedBy("forensics-api-plugin")
            .withPriority(20)
            .withScopes(Scope.CLASS)
            .build();

    @Override
    public MetricsProvider getMetricsProviderFor(final Run<?, ?> build) {
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
        var measurement = new ClassMetricsMeasurementBuilder();
        measurement.withFileName(entry.getKey());

        var report = entry.getValue();
        var first = report.get(0);
        measurement.withPackageName(first.getPackageName());
        measurement.withClassName(first.getBaseName().replace(".java", ""));

        measurement.withMetric(new IntegerMetric(ERRORS, report.getSizeOf(Severity.ERROR)));
        measurement.withMetric(new IntegerMetric(WARNINGS_HIGH, report.getSizeOf(Severity.WARNING_HIGH)));
        measurement.withMetric(new IntegerMetric(WARNINGS_NORMAL, report.getSizeOf(Severity.WARNING_NORMAL)));
        measurement.withMetric(new IntegerMetric(WARNINGS_LOW, report.getSizeOf(Severity.WARNING_LOW)));

        if (stats.contains(entry.getKey())) {
            var fileStatistics = stats.get(entry.getKey());
            measurement.withMetric(new IntegerMetric(AUTHORS, fileStatistics.getNumberOfAuthors()));
            measurement.withMetric(new IntegerMetric(COMMITS, fileStatistics.getNumberOfCommits()));
        }

        return measurement.build();
    }

    @Override
    public Set<MetricDefinition> getAvailableMetricsFor(final Run<?, ?> build) {
        var actions = build.getActions(ResultAction.class);
        if (actions.isEmpty()) {
            return Set.of();
        }

        // TODO: we should report by report ID as well
        return Set.of(ERRORS, WARNINGS_HIGH, WARNINGS_NORMAL, WARNINGS_LOW, AUTHORS, COMMITS);
    }
}
