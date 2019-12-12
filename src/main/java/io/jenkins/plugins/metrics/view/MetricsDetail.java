package io.jenkins.plugins.metrics.view;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.ExportedBean;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.metrics.extension.MetricsProviderFactory;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.MetricsProvider;
import io.jenkins.plugins.metrics.model.MetricsTreeNode;
import io.jenkins.plugins.metrics.util.JacksonFacade;

/**
 * Build view for metrics.
 *
 * @author Andreas Pabst
 */
@SuppressWarnings({"PMD.ExcessiveImports", "ClassDataAbstractionCoupling", "ClassFanOutComplexity"})
@ExportedBean
public class MetricsDetail implements ModelObject {
    private final Run<?, ?> owner;
    private final List<MetricsMeasurement> metricsMeasurements;
    private final List<Metric> supportedMetrics;
    private final List<String> projectOverview;
    private final Map<String, Double> metricsMaxima;

    public MetricsDetail(final Run<?, ?> owner) {
        this.owner = owner;
        metricsMeasurements = MetricsProviderFactory.getAllFor(owner.getAllActions()).stream()
                .map(MetricsProvider::getMetricsMeasurements)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(MetricsMeasurement::getQualifiedClassName))
                .values().stream()
                .map(perFileMeasurements -> perFileMeasurements.stream().reduce(MetricsMeasurement::merge)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        supportedMetrics = MetricsProviderFactory.getAllSupportedMetricsFor(owner.getAllActions());

        metricsMaxima = supportedMetrics.stream()
                .collect(Collectors.toMap(Metric::getId, metric -> metricsMeasurements.stream()
                        .map(metricsMeasurement -> metricsMeasurement.getMetric(metric.getId()))
                        .map(d -> d.orElse(0.0))
                        .filter(Double::isFinite)
                        .max(Double::compare)
                        .orElse(0.0))
                );

        projectOverview = MetricsProviderFactory.getAllFor(owner.getAllActions()).stream()
                .map(MetricsProvider::getProjectSummaryEntries)
                .reduce(new LinkedList<>(), (acc, summary) -> {
                    acc.addAll(summary);
                    return acc;
                });

    }

    @Override
    public String getDisplayName() {
        return "Metrics";
    }

    /**
     * Returns the build as owner of this object.
     *
     * @return the owner
     */
    @SuppressWarnings("unused") // used by jelly view
    public final Run<?, ?> getOwner() {
        return owner;
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public List<Metric> getSupportedMetrics() {
        return supportedMetrics;
    }

    @SuppressWarnings("unused") // used by jelly view
    public String getSupportedMetricsJSON() {
        return toJson(supportedMetrics);
    }

    @SuppressWarnings("unused") // used by jelly view
    public List<String> getProjectOverview() {
        return projectOverview;
    }

    @SuppressWarnings("unused") // used by jelly view
    public String getMetricsMaximaJSON() {
        return toJson(metricsMaxima);
    }

    private String toJson(final Object object) {
        JacksonFacade facade = new JacksonFacade();
        return facade.toJson(object);
    }

    @SuppressWarnings("unused") // used by jelly view
    public String getMetrics() {
        return toJson(metricsMeasurements);
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public MetricsTreeNode getMetricsTree(final String valueKey) {
        MetricsTreeNode root = metricsMeasurements.stream()
                .map(measurement -> {
                    double value = measurement.getMetric(valueKey).orElse(0.0);
                    if (!Double.isFinite(value)) {
                        value = 0.0;
                    }
                    return new MetricsTreeNode(measurement.getQualifiedClassName(), value);
                })
                .reduce(new MetricsTreeNode(""), (acc, node) -> {
                    acc.insertNode(node);
                    return acc;
                });

        root.collapsePackage();

        return root;
    }

    private List<Double> getAllMetrics(final String metricId) {
        return metricsMeasurements.stream()
                .map(m -> m.getMetric(metricId).orElse(Double.NaN))
                .filter(Double::isFinite)
                .collect(Collectors.toList());
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public String getHistogram(final String metricId) {
        List<Double> values = getAllMetrics(metricId);

        final int numBins = 10;
        final int[] histogramData = new int[numBins];
        final double min = values.stream().min(Double::compareTo).orElse(0.0);
        final double max = values.stream().max(Double::compareTo).orElse(0.0);
        final double binWidth = (max - min) / numBins;

        for (double v : values) {
            int binId = (int) ((v - min) / binWidth);
            if (binId < 0) {
                binId = 0;
            }
            else if (binId >= numBins) {
                binId = numBins - 1;
            }

            histogramData[binId] += 1;
        }

        final DecimalFormat labelFormat = new DecimalFormat("#.##");
        final String[] binLabels = new String[numBins];
        for (int i = 0; i < numBins; i++) {
            double left = min + i * binWidth;
            double right = min + (i + 1) * binWidth;
            binLabels[i] = String.format("%s - %s", labelFormat.format(left), labelFormat.format(right));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", histogramData);
        result.put("labels", binLabels);
        return toJson(result);
    }
}
