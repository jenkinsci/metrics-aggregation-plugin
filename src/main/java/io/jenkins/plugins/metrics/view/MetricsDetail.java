package io.jenkins.plugins.metrics.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.collections.impl.factory.Sets;

import com.google.common.collect.Lists;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.ExportedBean;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.coverage.CoverageAction;
import io.jenkins.plugins.coverage.targets.CoverageElement;
import io.jenkins.plugins.coverage.targets.CoverageResult;
import io.jenkins.plugins.coverage.targets.Ratio;
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
    private final Set<Metric> supportedMetrics;
    private final Map<Metric, Double> projectOverview;

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

        supportedMetrics = MetricsProviderFactory.all()
                .stream()
                .map(MetricsProviderFactory::supportedMetrics)
                .reduce(Sets.mutable.empty(), (acc, metrics) -> {
                    acc.addAll(metrics);
                    return acc;
                });

        projectOverview = MetricsProviderFactory.getAllFor(owner.getAllActions()).stream()
                .map(MetricsProvider::getProjectSummary)
                .reduce(new HashMap<>(), (acc, summary) -> {
                    acc.putAll(summary);
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
    public Set<Metric> getSupportedMetrics() {
        return supportedMetrics;
    }

    @SuppressWarnings("unused") // used by jelly view
    public String getSupportedMetricsJSON() {
        return toJson(supportedMetrics);
    }

    @SuppressWarnings("unused") // used by jelly view
    public Map<Metric, Double> getProjectOverview() {
        return projectOverview;
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
                .map(measurement -> new MetricsTreeNode(
                        measurement.getQualifiedClassName(),
                        measurement.getMetric(valueKey).orElse(0.0)))
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

        final String[] binLabels = new String[numBins];
        for (int i = 0; i < numBins; i++) {
            double left = min + i * binWidth;
            double right = min + (i + 1) * binWidth;
            binLabels[i] = String.format("%.1f - %.1f", left, right);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", histogramData);
        result.put("labels", binLabels);
        return toJson(result);
    }

    /*------------------ COVERAGE ------------------*/

    private static final String COVERAGE_NAMES = "package,basename,classcoverage,methodcoverage,instructioncoverage,conditionalcoverage,linecoverage";

    // returns csv
    public String getCoverage() {
        return COVERAGE_NAMES + "\n"
                + owner.getActions(CoverageAction.class)
                .stream()
                .map(CoverageAction::getResult)
                .map(this::getChildrenRecursive)
                .flatMap(List::stream)
                .filter(r -> r.getElement().is("File"))
                .map(this::coverageAsCSV)
                .collect(Collectors.joining("\n"));
    }

    String toCSV(final String... values) {
        final String quote = "\"";
        final String separator = quote + "," + quote;

        return quote + String.join(separator, values) + quote;
    }

    private String coverageAsCSV(final CoverageResult result) {
        return toCSV(
                normalizePackageName(result.getParent().getName()),
                result.getName(),
                ratioToString(result.getCoverage(CoverageElement.get("Class"))),
                ratioToString(result.getCoverage(CoverageElement.get("Method"))),
                ratioToString(result.getCoverage(CoverageElement.get("Instruction"))),
                ratioToString(result.getCoverage(CoverageElement.get("Conditional"))),
                ratioToString(result.getCoverage(CoverageElement.get("Line")))
        );
    }

    private String normalizePackageName(final String packageName) {
        return packageName != null ? packageName.replaceAll("/", ".") : "";
    }

    private String ratioToString(final Ratio ratio) {
        if (ratio == null) {
            return "";
        }
        return ratio.getPercentageString();
    }

    private List<CoverageResult> getChildrenRecursive(final CoverageResult result) {
        List<CoverageResult> children = Lists.newArrayList(result);

        for (CoverageResult res : result.getChildrenReal().values()) {
            children.addAll(getChildrenRecursive(res));
        }

        return children;
    }
}
