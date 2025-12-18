package io.jenkins.plugins.metrics.extension;

import edu.hm.hafner.coverage.Metric;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import hudson.Extension;
import hudson.model.Run;

import io.jenkins.plugins.coverage.metrics.steps.CoverageBuildAction;
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
    private static final MetricDefinition INSTRUCTION_COVERAGE = new MetricDefinitionBuilder("INSTRUCTION_COVERAGE")
            .withDisplayName("Instruction Coverage (%)")
            .withDescription("The percentage of covered instructions")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("INSTRUCTION")
            .withKindOfValue(PercentageMetric.class)
            .build();
    private static final MetricDefinition MCDC_PAIR_COVERAGE = new MetricDefinitionBuilder("MCDC_PAIR_COVERAGE")
            .withDisplayName("Modified Condition and Decision Coverage (%)")
            .withDescription("The percentage of covered Modified Condition and Decision")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("MCDC_PAIR")
            .withKindOfValue(PercentageMetric.class)
            .build();
    private static final MetricDefinition FUNCTION_CALL_COVERAGE = new MetricDefinitionBuilder("FUNCTION_CALL_COVERAGE")
            .withDisplayName("Function Call Coverage (%)")
            .withDescription("The percentage of covered function calls")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("FUNCTION_CALL")
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
    private static final MetricDefinition TEST_STRENGTH = new MetricDefinitionBuilder("TEST_STRENGTH")
            .withDisplayName("Test Strength (%)")
            .withDescription("The percentage of test strength")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("TEST_STRENGTH")
            .withKindOfValue(PercentageMetric.class)
            .build();
    private static final MetricDefinition TESTS_NUMBER = new MetricDefinitionBuilder("TESTS_NUMBER")
            .withDisplayName("Number of Tests")
            .withDescription("The number of Tests")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("TESTS")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition LOC = new MetricDefinitionBuilder("LOC")
            .withDisplayName("Lines of Code")
            .withDescription("The number of lines of code")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("LOC")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition NCSS = new MetricDefinitionBuilder("NCSS")
            .withDisplayName("Non Commenting Source Statements")
            .withDescription("The number of non commenting source statements")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("NCSS")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition CYCLOMATIC_COMPLEXITY = new MetricDefinitionBuilder("CYCLOMATIC_COMPLEXITY")
            .withDisplayName("Cyclomatic Complexity")
            .withDescription("The cyclomatic complexity")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("CYCLOMATIC_COMPLEXITY")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition COGNITIVE_COMPLEXITY = new MetricDefinitionBuilder("COGNITIVE_COMPLEXITY")
            .withDisplayName("Cognitive Complexity")
            .withDescription("The cognitive complexity")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("COGNITIVE_COMPLEXITY")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition NPATH_COMPLEXITY = new MetricDefinitionBuilder("NPATH_COMPLEXITY")
            .withDisplayName("N-Path Complexity")
            .withDescription("The n-path complexity")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("NPATH_COMPLEXITY")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition ACCESS_TO_FOREIGN_DATA = new MetricDefinitionBuilder("ACCESS_TO_FOREIGN_DATA")
            .withDisplayName("Access to Foreign Data")
            .withDescription("The number of access to foreign data")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("ACCESS_TO_FOREIGN_DATA")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition COHESION = new MetricDefinitionBuilder("COHESION")
            .withDisplayName("Class Cohesion (%)")
            .withDescription("The percetage of class coheison")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("COHESION")
            .withKindOfValue(PercentageMetric.class)
            .build();
    private static final MetricDefinition FAN_OUT = new MetricDefinitionBuilder("FAN_OUT")
            .withDisplayName("Fan Out")
            .withDescription("The number of fan out")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("FAN_OUT")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition NUMBER_OF_ACCESSORS = new MetricDefinitionBuilder("NUMBER_OF_ACCESSORS")
            .withDisplayName("Number of Accessors")
            .withDescription("The number accessors")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("NUMBER_OF_ACCESSORS")
            .withKindOfValue(IntegerMetric.class)
            .build();
    private static final MetricDefinition WEIGHT_OF_CLASS = new MetricDefinitionBuilder("WEIGHT_OF_CLASS")
            .withDisplayName("Weight of Class (%)")
            .withDescription("The weight of class")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("WEIGHT_OF_CLASS")
            .withKindOfValue(PercentageMetric.class)
            .build();
    private static final MetricDefinition WEIGHED_METHOD_COUNT = new MetricDefinitionBuilder("WEIGHED_METHOD_COUNT")
            .withDisplayName("Weighted Method Count")
            .withDescription("The count of weighted method")
            .withReportedBy("code-coverage-plugin")
            .withPriority(10)
            .withScopes(Scope.CLASS)
            .withOriginalLabel("WEIGHED_METHOD_COUNT")
            .withKindOfValue(IntegerMetric.class)
            .build();

    @Override
    protected MetricsProvider getMetricsProviderFor(final Run<?, ?> build) {
        Map<String, ClassMetricsMeasurementBuilder> builders = new HashMap<>();
        Map<String, Set<String>> seenMetricIds = new HashMap<>();
        var provider = new MetricsProvider();
        provider.setOrigin("code-coverage-plugin");
        var actions = build.getActions(CoverageBuildAction.class);

        for (CoverageBuildAction action : actions) {
            var root = action.getResult();

            for (var classNode : root.getAllClassNodes()) {
                var className = classNode.getName();

                var builder = builders.computeIfAbsent(
                        className,
                        n -> new ClassMetricsMeasurementBuilder()
                                .withClassName(n)
                                .withFileName("fileNamePlaceholder")
                                .withPackageName(classNode.getPackageName())
                );

                var seen = seenMetricIds.computeIfAbsent(className, k -> new HashSet<>());

                for (var metricDefinition : getAvailableMetricsFor(build)) {
                    var cov = classNode.getValue(Metric.valueOf(metricDefinition.getOriginalLabel()));

                    if (cov.isPresent() && !seen.contains(metricDefinition.getId())) {
                        seen.add(metricDefinition.getId());
                        var kindOfValueClass = metricDefinition.getKindOfValue();
                        if (kindOfValueClass == DoubleMetric.class) {
                            var covered = cov.get().asDouble();
                            builder.withMetric(new DoubleMetric(metricDefinition, cov.get().asDouble()));
                        }
                        else if (kindOfValueClass == PercentageMetric.class) {
                            builder.withMetric(new PercentageMetric(metricDefinition, cov.get().asDouble()));
                        }
                        else if (kindOfValueClass == IntegerMetric.class) {
                            builder.withMetric(new IntegerMetric(metricDefinition, cov.get().asInteger()));
                        }
                    }
                }
            }
        }
        List<MetricsMeasurement> list = builders.values().stream()
                .map(ClassMetricsMeasurementBuilder::build)
                .collect(Collectors.toList());
        provider.setMetricsMeasurements(list);
        return provider;
    }

    @Override
    public Set<MetricDefinition> getAvailableMetricsFor(final Run<?, ?> build) {
        var actions = build.getActions(CoverageBuildAction.class);
        if (actions.isEmpty()) {
            return Set.of();
        }
        return Set.of(LINE_COVERAGE, BRANCH_COVERAGE, INSTRUCTION_COVERAGE, MCDC_PAIR_COVERAGE, FUNCTION_CALL_COVERAGE,
                MUTATION_COVERAGE, TEST_STRENGTH, TESTS_NUMBER, LOC, NCSS, CYCLOMATIC_COMPLEXITY, COGNITIVE_COMPLEXITY,
                NPATH_COMPLEXITY, ACCESS_TO_FOREIGN_DATA, COHESION, FAN_OUT, NUMBER_OF_ACCESSORS, WEIGHT_OF_CLASS,
                WEIGHED_METHOD_COUNT);
    }
}