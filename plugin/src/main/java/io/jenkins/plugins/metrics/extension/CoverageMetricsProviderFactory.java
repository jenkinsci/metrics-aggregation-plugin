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
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.model.metric.PercentageMetric;

@Extension
@SuppressWarnings("unused") // used via the extension
public class CoverageMetricsProviderFactory extends MetricsProviderFactory<CoverageAction> {

    private static final MetricDefinition METHOD = new MetricDefinition("METHOD_COVERAGE",
            "Method coverage",
            "TODO",
            "code-coverage-api",
            30,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition INSTRUCTION = new MetricDefinition("INSTRUCTION_COVERAGE",
            "Instruction coverage",
            "TODO",
            "code-coverage-api",
            30,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition CONDITIONAL = new MetricDefinition("CONDITIONAL_COVERAGE",
            "Conditional coverage",
            "TODO",
            "code-coverage-api",
            30,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition LINE = new MetricDefinition("LINE_COVERAGE",
            "Line coverage",
            "TODO",
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
        provider.setOrigin("warnings-ng-plugin");

        provider.setMetricsMeasurements(actions.stream()
                .map(CoverageAction::getResult)
                .map(this::getChildrenRecursive)
                .flatMap(List::stream)
                .filter(r -> r.getElement().is("File"))
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
                        }
                    }
                    return acc;
                });

        overallCoverage.ifPresent(coverageResult -> {
            Ratio classCoverage = coverageResult.getCoverage(CoverageElement.get("Class"));
            if (classCoverage != null) {
                provider.addProjectSummaryEntry(
                        String.format("class coverage: %s classes (%d%%)", classCoverage.toString(),
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
        ClassMetricsMeasurement metricsMeasurement = new ClassMetricsMeasurement();
        metricsMeasurement.setPackageName(normalizePackageName(result.getParent().getName()));
        metricsMeasurement.setClassName(result.getName().replace(".java", ""));
        metricsMeasurement.addMetric(new PercentageMetric(METHOD, getCoverage(result, "Method")));
        metricsMeasurement.addMetric(new PercentageMetric(INSTRUCTION, getCoverage(result, "Instruction")));
        metricsMeasurement.addMetric(new PercentageMetric(CONDITIONAL, getCoverage(result, "Conditional")));
        metricsMeasurement.addMetric(new PercentageMetric(LINE, getCoverage(result, "Line")));
        return metricsMeasurement;
    }

    private float getCoverage(final CoverageResult result, final String id) {
        Ratio ratio = result.getCoverage(CoverageElement.get("Instruction"));
        if (ratio != null) {
            return ratio.getPercentageFloat();
        }
        else {
            return Float.NaN;
        }
    }

    private String normalizePackageName(final String packageName) {
        return packageName != null ? packageName.replaceAll("/", ".") : "";
    }
}
