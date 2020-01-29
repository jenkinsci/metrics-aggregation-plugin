package io.jenkins.plugins.metrics.view;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.ExportedBean;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.metrics.extension.MetricsProviderFactory;
import io.jenkins.plugins.metrics.model.MetricsProvider;
import io.jenkins.plugins.metrics.model.MetricsTreeNode;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MethodMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.util.JacksonFacade;

/**
 * Build view for displaying metrics.
 *
 * @author Andreas Pabst
 */
@ExportedBean
public class MetricsView extends DefaultAsyncTableContentProvider implements ModelObject {
    private final Run<?, ?> owner;
    private final List<ClassMetricsMeasurement> metricsMeasurements;
    private final List<MetricDefinition> supportedMetrics;
    private final List<String> projectOverview;
    private final Map<String, Double> metricsMaxima;

    public MetricsView(final Run<?, ?> owner) {
        this.owner = owner;
        metricsMeasurements = MetricsProviderFactory.getAllFor(owner.getAllActions()).stream()
                .map(MetricsProvider::getMetricsMeasurements)
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(MetricsMeasurement::getQualifiedClassName))
                .values().stream()
                .map(measurementsPerFile -> measurementsPerFile.stream().reduce(MetricsMeasurement::merge)
                        .orElse(null))
                .map(measurement -> {
                    if (measurement instanceof ClassMetricsMeasurement) {
                        return measurement;
                    }
                    else if (measurement instanceof MethodMetricsMeasurement) {
                        MethodMetricsMeasurement methodMetricsMeasurement = (MethodMetricsMeasurement) measurement;
                        ClassMetricsMeasurement classMetricsMeasurement = methodMetricsMeasurement.getParent();
                        if (classMetricsMeasurement == null) {
                            //TODO no parent but method???
                            return methodMetricsMeasurement;
                        }
                        methodMetricsMeasurement.setParent(null);
                        classMetricsMeasurement.addChild(measurement);
                        return classMetricsMeasurement;
                    }
                    else {
                        return null;
                    }
                })
                .filter(m -> m instanceof ClassMetricsMeasurement)
                .map(m -> (ClassMetricsMeasurement) m)
                .collect(Collectors.toList());

        supportedMetrics = MetricsProviderFactory.getAllSupportedMetricsFor(owner.getAllActions())
                .stream()
                .filter(metricDefinition -> metricDefinition.validForScope(Scope.CLASS))
                .collect(Collectors.toList());

        metricsMaxima = supportedMetrics.stream()
                .collect(Collectors.toMap(MetricDefinition::getId, metric -> metricsMeasurements.stream()
                        .map(metricsMeasurement -> metricsMeasurement.getMetric(metric.getId()))
                        .map(d -> d.orElse(0.0).doubleValue())
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
        return Messages.metrics();
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
    public List<MetricDefinition> getSupportedMetrics() {
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

    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public String getMetricsJSON() {
        //return toJson(metricsMeasurements);
        return toJson(new MetricsTableModel("metrics-table", supportedMetrics, metricsMeasurements));
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public MetricsTreeNode getMetricsTree(final String valueKey) {
        MetricsTreeNode root = metricsMeasurements.stream()
                .map(measurement -> {
                    double value = measurement.getMetric(valueKey).orElse(0.0).doubleValue();
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
                .map(Number::doubleValue)
                .filter(Double::isFinite)
                .collect(Collectors.toList());
    }

    private boolean isIntegerMetric(final String metricId) {
        Optional<Metric> metric = metricsMeasurements.stream()
                .map(m -> m.getMetrics().get(metricId))
                .filter(Objects::nonNull)
                .findFirst();

        return metric.isPresent() && metric.get() instanceof IntegerMetric;
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public String getHistogram(final String metricId) {
        List<Double> values = getAllMetrics(metricId);

        if (values.isEmpty()) {
            return "{\"data\": [], \"labels\":[]}";
        }

        DescriptiveStatistics statistics = new DescriptiveStatistics();
        values.forEach(statistics::addValue);

        final double min = statistics.getMin();
        final double max = statistics.getMax();
        // Freedman-Diaconis rule for calculating the binWidth
        final double IQR = statistics.getPercentile(75) - statistics.getPercentile(25);

        final int numBins;
        double binWidth;
        if (IQR > 0) {
            binWidth = (2 * IQR) / Math.cbrt(values.size());
            numBins = (int) Math.round((max - min) / binWidth);
        }
        else if (max - min > 0) {
            // fall back to Sturges rule, if the binWidth would become 0
            numBins = (int) (1 + Math.log(values.size()) / Math.log(2));
            binWidth = (max - min) / numBins;
        }
        else {
            // IQR == 0 and min == max -> all datapoints are the same -> use just a single
            numBins = 1;
            binWidth = 1;
        }

        // round the binWidth, if an integer metric is requested
        if (isIntegerMetric(metricId)) {
            binWidth = Math.round(binWidth);
            // the binWidth should not be smaller than 1
            if (binWidth < 1) {
                binWidth = 1;
            }
        }

        final int[] histogramData = new int[numBins];
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

    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public String getScatterPlot(final String metricId, final String secondMetricId) {
        List<ScatterPlotDataItem> data = metricsMeasurements.stream()
                .map(m -> new ScatterPlotDataItem(m.getClassName(),
                        m.getMetric(metricId).orElse(Double.NaN),
                        m.getMetric(secondMetricId).orElse(Double.NaN)))
                .collect(Collectors.toList());

        return toJson(data);
    }
    
    private String toJson(final Object object) {
        JacksonFacade facade = new JacksonFacade();
        return facade.toJson(object);
    }

    /**
     * Returns a new sub page for the selected link.
     *
     * @param link
     *         the link to identify the sub page to show
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the new sub page
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new ClassDetailsView(owner, link);
    }

    @Override
    public TableModel getTableModel(final String id) {
        return new MetricsTableModel(id, supportedMetrics, metricsMeasurements);
    }

    private static final class ScatterPlotDataItem {
        private final String name;
        private final List<Number> value;

        private ScatterPlotDataItem(final String name, final List<Number> value) {
            this.name = name;
            this.value = value;
        }

        private ScatterPlotDataItem(final String name, final Number... values) {
            this.name = name;
            this.value = Arrays.asList(values);
        }

        public String getName() {
            return name;
        }

        public List<Number> getValue() {
            return value;
        }
    }
}
