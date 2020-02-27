package io.jenkins.plugins.metrics.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;

import hudson.Extension;

import io.jenkins.plugins.coverage.CoverageAction;
import io.jenkins.plugins.coverage.exception.CoverageException;
import io.jenkins.plugins.coverage.targets.CoverageElement;
import io.jenkins.plugins.coverage.targets.CoverageResult;
import io.jenkins.plugins.coverage.targets.Ratio;
import io.jenkins.plugins.metrics.model.MetricsProvider;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MethodMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.model.metric.PercentageMetric;

/**
 * {@link MetricsProviderFactory} for coverage metrics. Data fetched from the code-coverage-api-plugin.
 */
@Extension
@SuppressWarnings("unused") // used via the extension
public class CoverageMetricsProviderFactory extends MetricsProviderFactory<CoverageAction> {

    private static final MetricDefinition METHOD = new MetricDefinition("METHOD_COVERAGE",
            "Method coverage",
            "The percentage of methods in a class, that are covered by tests.",
            "code-coverage-api",
            30,
            ArrayUtils.toArray(Scope.CLASS));
    private static final MetricDefinition INSTRUCTION = new MetricDefinition("INSTRUCTION_COVERAGE",
            "Instruction coverage",
            "The percentage of instructions, that are covered by tests.",
            "code-coverage-api",
            30,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition CONDITIONAL = new MetricDefinition("CONDITIONAL_COVERAGE",
            "Conditional coverage",
            "The percentage of conditions, that are covered by tests.",
            "code-coverage-api",
            30,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition LINE = new MetricDefinition("LINE_COVERAGE",
            "Line coverage",
            "The percentage of lines, that are covered by tests.",
            "code-coverage-api",
            30,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));

    @Override
    public Class<CoverageAction> type() {
        return CoverageAction.class;
    }

    @Override
    public MetricsProvider getFor(final List<CoverageAction> actions) {
        MetricsProvider provider = new MetricsProvider();
        provider.setOrigin("code-coverage-api-plugin");

        provider.setMetricsMeasurements(actions.stream()
                .map(CoverageAction::getResult)
                .map(this::getChildrenRecursive)
                .flatMap(List::stream)
                .filter(result -> {
                    CoverageElement element = result.getElement();
                    return element.is("Class") || element.is("Method");
                })
                .map(this::coverageToMetricsMeasurement)
                .collect(Collectors.toList())
        );

        Optional<CoverageResult> overallCoverage = actions.stream()
                .map(CoverageAction::getResult)
                .reduce((acc, result) -> {
                    if (result != null) {
                        try {
                            acc.merge(result);
                        }
                        catch (CoverageException ignored) {
                            //ignored
                        }
                    }
                    return acc;
                });

        overallCoverage.ifPresent(coverageResult -> {
            Ratio classCoverage = coverageResult.getCoverage(CoverageElement.get("Class"));
            if (classCoverage != null) {
                provider.addProjectSummaryEntry(
                        String.format("Class Coverage: %s Classes (%d%%)", classCoverage.toString(),
                                classCoverage.getPercentage()));
            }
        });

        return provider;
    }

    @Override
    public ArrayList<MetricDefinition> supportedMetricsFor(final List<CoverageAction> actions) {
        if (actions.isEmpty()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(Arrays.asList(METHOD, INSTRUCTION, CONDITIONAL, LINE));
    }

    private List<CoverageResult> getChildrenRecursive(final CoverageResult result) {
        List<CoverageResult> children = Lists.newArrayList(result);

        for (CoverageResult res : result.getChildrenReal().values()) {
            children.addAll(getChildrenRecursive(res));
        }

        return children;
    }

    private MetricsMeasurement coverageToMetricsMeasurement(final CoverageResult result) {
        CoverageElement element = result.getElement();
        MetricsMeasurement metricsMeasurement;
        if (element.is("Class")) {
            metricsMeasurement = new ClassMetricsMeasurement();
            final String name = normalizeClassName(result.getName());
            final int lastDot = name.lastIndexOf(".");
            metricsMeasurement.setPackageName(name.substring(0, lastDot));
            metricsMeasurement.setClassName(name.substring(lastDot + 1));

            metricsMeasurement.addMetric(new PercentageMetric(METHOD, getCoverage(result, "Method")));
        }
        else if (element.is("Method")) {
            metricsMeasurement = new MethodMetricsMeasurement();
            final String parentName = normalizeClassName(result.getParent().getName());
            final int lastDot = parentName.lastIndexOf(".");
            final String className = parentName.substring(lastDot + 1);
            metricsMeasurement.setPackageName(parentName.substring(0, lastDot));
            metricsMeasurement.setClassName(className);

            final String name = result.getName().replace("<init>", className);
            ((MethodMetricsMeasurement) metricsMeasurement).setMethodName(name);
        }
        else {
            return null;
        }

        metricsMeasurement.addMetric(new PercentageMetric(INSTRUCTION, getCoverage(result, "Instruction")));
        metricsMeasurement.addMetric(new PercentageMetric(CONDITIONAL, getCoverage(result, "Conditional")));
        metricsMeasurement.addMetric(new PercentageMetric(LINE, getCoverage(result, "Line")));

        return metricsMeasurement;
    }

    private float getCoverage(final CoverageResult result, final String id) {
        Ratio ratio = result.getCoverage(CoverageElement.get(id));
        if (ratio != null) {
            return ratio.getPercentageFloat();
        }
        else {
            return Float.NaN;
        }
    }

    private String normalizeClassName(final String packageName) {
        return packageName != null ? packageName.replaceAll("/", ".") : "";
    }
}
