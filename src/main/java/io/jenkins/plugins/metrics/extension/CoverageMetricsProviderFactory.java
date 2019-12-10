package io.jenkins.plugins.metrics.extension;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import hudson.Extension;

import io.jenkins.plugins.coverage.CoverageAction;
import io.jenkins.plugins.coverage.exception.CoverageException;
import io.jenkins.plugins.coverage.targets.CoverageElement;
import io.jenkins.plugins.coverage.targets.CoverageResult;
import io.jenkins.plugins.coverage.targets.Ratio;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.MetricsProvider;

@Extension
@SuppressWarnings("unused") // used via the extension
public class CoverageMetricsProviderFactory extends MetricsProviderFactory<CoverageAction> {

    private static final Metric METHOD = new Metric("METHOD_COVERAGE", "Method coverage",
            "TODO", "code-coverage-api");
    private static final Metric INSTRUCTION = new Metric("INSTRUCTION_COVERAGE", "Instruction coverage",
            "TODO", "code-coverage-api");
    private static final Metric CONDITIONAL = new Metric("CONDITIONAL_COVERAGE", "Conditional coverage",
            "TODO", "code-coverage-api");
    private static final Metric LINE = new Metric("LINE_COVERAGE", "Line coverage",
            "TODO", "code-coverage-api");

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
            provider.addProjectSummaryEntry(String.format("Total class coverage: %s Classes (%d%%)", classCoverage.toString(),
                    classCoverage.getPercentage()));
        });

        return provider;
    }

    @Override
    public LinkedHashSet<Metric> supportedMetricsFor(final List<CoverageAction> actions) {
        if (actions.isEmpty()) {
            return new LinkedHashSet<>();
        }

        return new LinkedHashSet<>(Arrays.asList(METHOD, INSTRUCTION, CONDITIONAL, LINE));
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
        metricsMeasurement.addMetric(METHOD, getCoverage(result, "Method"));
        metricsMeasurement.addMetric(INSTRUCTION, getCoverage(result, "Instruction"));
        metricsMeasurement.addMetric(CONDITIONAL, getCoverage(result, "Conditional"));
        metricsMeasurement.addMetric(LINE, getCoverage(result, "Line"));
        return metricsMeasurement;
    }

    private double getCoverage(final CoverageResult result, final String id) {
        Ratio ratio = result.getCoverage(CoverageElement.get("Instruction"));
        if (ratio != null) {
            return ratio.getPercentageFloat();
        }
        else {
            return Double.NaN;
        }
    }

    private String normalizePackageName(final String packageName) {
        return packageName != null ? packageName.replaceAll("/", ".") : "";
    }
}
