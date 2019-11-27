package io.jenkins.plugins.metrics.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import hudson.Extension;

import io.jenkins.plugins.analysis.core.model.AnalysisResult;
import io.jenkins.plugins.analysis.core.model.ResultAction;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.MetricsProvider;

@Extension
@SuppressWarnings("unused") // used via the extension
public class WarningsMetricsProvider extends MetricsProviderFactory<ResultAction> {

    @Override
    public Class<ResultAction> type() {
        return ResultAction.class;
    }

    @Override
    public MetricsProvider getFor(final List<ResultAction> actions) {
        MetricsProvider provider = new MetricsProvider();
        provider.setOrigin("warnings-ng-plugin");
        
        List<MetricsMeasurement> metricsMeasurements = actions.stream()
                .map(ResultAction::getResult)
                .map(AnalysisResult::getIssues)
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

                    measurement.addMetric(new Metric("ERRORS",
                            "Errors",
                            "An error, e.g. a compile error.",
                            report.getSizeOf(Severity.ERROR)));

                    measurement.addMetric(new Metric("WARNING_HIGH",
                            "Warning (high)",
                            "A warning with priority high.",
                            report.getSizeOf(Severity.WARNING_HIGH)));

                    measurement.addMetric(new Metric("WARNING_NORMAL",
                            "Warning (normal)",
                            "A warning with priority normal.",
                            report.getSizeOf(Severity.WARNING_NORMAL)));

                    measurement.addMetric(new Metric("WARNING_LOW",
                            "Warning (low)",
                            "A warning with priority low.",
                            report.getSizeOf(Severity.WARNING_LOW)));

                    return measurement;
                })
                .collect(Collectors.toList());

        provider.setMetricsMeasurements(metricsMeasurements);
        return provider;
    }
}
