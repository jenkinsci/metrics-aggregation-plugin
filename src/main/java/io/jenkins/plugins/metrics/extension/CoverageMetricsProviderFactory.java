package io.jenkins.plugins.metrics.extension;

import edu.hm.hafner.coverage.Metric;

import java.util.ArrayList;
import java.util.Set;

import hudson.Extension;
import hudson.model.Run;

import io.jenkins.plugins.coverage.metrics.steps.CoverageBuildAction;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement.ClassMetricsMeasurementBuilder;
import io.jenkins.plugins.metrics.model.DoubleMetric;
import io.jenkins.plugins.metrics.model.IntegerMetric;
import io.jenkins.plugins.metrics.model.MetricDefinition;
import io.jenkins.plugins.metrics.model.MetricDefinition.MetricDefinitionBuilder;
import io.jenkins.plugins.metrics.model.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.PercentageMetric;

/**
 * {@link MetricsProviderFactory} for the coverage data. Uses the code-coverage-plugin.
 */
@Extension
@SuppressWarnings("unused") // used via the extension
public class CoverageMetricsProviderFactory extends MetricsProviderFactory {
    private static final MetricDefinition LINE_COVERAGE = new MetricDefinitionBuilder("LINE_COVERAGE")
            .withDisplayName("Line Coverage (%)")
            .withDescription("The percentage of covered lines")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("LINE")
            .withKindOfValue(PercentageMetric.class)
            .build();
    private static final MetricDefinition BRANCH_COVERAGE = new MetricDefinitionBuilder("BRANCH_COVERAGE")
            .withDisplayName("Branch Coverage (%)")
            .withDescription("The percentage of covered branches")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("BRANCH")
            .withKindOfValue(PercentageMetric.class)
            .build();
    private static final MetricDefinition MUTATION_COVERAGE = new MetricDefinitionBuilder("MUTATION_COVERAGE")
            .withDisplayName("Mutation Coverage (%)")
            .withDescription("The percentage of covered mutations")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("MUTATION")
            .withKindOfValue(PercentageMetric.class)
            .build();
    private static final MetricDefinition CYCLOMATIC_COMPLEXITY = new MetricDefinitionBuilder("CYCLOMATIC_COMPLEXITY")
            .withDisplayName("Cyclomatic Complexity (%)")
            .withDescription("The cyclomatic complexity")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("CYCLOMATIC_COMPLEXITY")
            .withKindOfValue(IntegerMetric.class)
            .build();

    @Override
    protected MetricsProvider getMetricsProviderFor(final Run<?, ?> build) {
        var provider = new MetricsProvider();
        provider.setOrigin("code-coverage-plugin");
        var actions = build.getActions(CoverageBuildAction.class);
        var measurements = new ArrayList<MetricsMeasurement>();

        for (CoverageBuildAction action : actions) {
            var root = action.getResult();

            for (var classNode : root.getAllClassNodes()) {
                for (var metricLabel : getAvailableMetricsFor(build)) {
                    var cov = classNode.getValue(Metric.valueOf(metricLabel.getOriginalLabel()));
                    ClassMetricsMeasurement metricCovered = null;
                    if (cov.isPresent()) {
                        var kindOfValueClass = metricLabel.getKindOfValue();
                        if (kindOfValueClass == DoubleMetric.class) {
                            var covered = cov.get().asDouble();
                            var builder = new ClassMetricsMeasurementBuilder();
                            metricCovered = builder.withMetric(new DoubleMetric(metricLabel, covered))
                                    .withClassName(classNode.getName())
                                    .withFileName("fileNamePlaceholder")
                                    .withPackageName(classNode.getPackageName())
                                    .build();
                        }
                        else if (kindOfValueClass == PercentageMetric.class) {
                            var covered = cov.get().asDouble();
                            var builder = new ClassMetricsMeasurementBuilder();
                            metricCovered = builder.withMetric(new PercentageMetric(metricLabel, covered))
                                    .withClassName(classNode.getName())
                                    .withFileName("fileNamePlaceholder")
                                    .withPackageName(classNode.getPackageName())
                                    .build();
                        }
                        else if (kindOfValueClass == IntegerMetric.class) {
                            var covered = cov.get().asInteger();
                            var builder = new ClassMetricsMeasurementBuilder();
                            metricCovered = builder.withMetric(new IntegerMetric(metricLabel, covered))
                                    .withClassName(classNode.getName())
                                    .withFileName("fileNamePlaceholder")
                                    .withPackageName(classNode.getPackageName())
                                    .build();
                        }
                        measurements.add(metricCovered);
                    }
                }
            }
        }
        provider.setMetricsMeasurements(measurements);
        return provider;
    }

    @Override
    public Set<MetricDefinition> getAvailableMetricsFor(final Run<?, ?> build) {
        var actions = build.getActions(CoverageBuildAction.class);
        if (actions.isEmpty()) {
            return Set.of();
        }
        return Set.of(LINE_COVERAGE, BRANCH_COVERAGE, MUTATION_COVERAGE);
    }
}