package io.jenkins.plugins.metrics.view;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.ExportedBean;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.metrics.extension.MetricsProvider;
import io.jenkins.plugins.metrics.extension.MetricsProviderFactory;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

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

    /**
     * Create a new {@link MetricsView}.
     *
     * @param build
     *         the {@link Run} that is shown in the view
     */
    public MetricsView(final Run<?, ?> build) {
        this.owner = build;
        metricsMeasurements = MetricsProviderFactory.findAllFor(build).stream()
                .map(MetricsProvider::getMetricsMeasurements)
                .flatMap(List::stream)
                .filter(ClassMetricsMeasurement.class::isInstance)
                .collect(Collectors.groupingBy(MetricsMeasurement::getQualifiedClassName))
                .values().stream()
                .map(measurementsPerFile -> (ClassMetricsMeasurement) measurementsPerFile.stream()
                        .reduce(MetricsMeasurement::merge).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        supportedMetrics = MetricsProviderFactory.findAllAvailableMetricsFor(build)
                .stream()
                .filter(metricDefinition -> metricDefinition.validForScope(Scope.CLASS))
                .collect(Collectors.toList());

        projectOverview = MetricsProviderFactory.findAllFor(build).stream()
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

    /**
     * Get all metrics for the table as JSON.
     *
     * @return the JSON string
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public String getMetricsJSON() {
        //return toJson(metricsMeasurements);
        return toJson(new MetricsTableModel("metrics-table", supportedMetrics, metricsMeasurements));
    }

    /**
     * Get a tree consisting of {@link MetricsTreeNode}s for a specific metric.
     *
     * @param metricId
     *         the id of the metric to show
     *
     * @return a string containing the tree's JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public String getMetricsTree(final String metricId) {
        List<MetricsTreeNode> nodes = metricsMeasurements.stream()
                .map(measurement -> {
                    double value = measurement.getMetric(metricId).orElse(0.0).doubleValue();
                    if (!Double.isFinite(value)) {
                        value = 0.0;
                    }
                    return new MetricsTreeNode(measurement.getQualifiedClassName(), value);
                })
                .collect(Collectors.toList());

        var root = new MetricsTreeNode("");
        nodes.forEach(root::insertNode);
        root.collapsePackage();

        return toJson(root);
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

    /**
     * Get the histogram for a specific metric.
     *
     * @param metricId
     *         the id of the metric to show
     *
     * @return the contents of the histogram as JSON
     */
    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public String getHistogram(final String metricId) {
        List<Double> values = getAllMetrics(metricId);

        if (values.isEmpty()) {
            return "{\"data\": [], \"labels\":[]}";
        }

        var statistics = new DescriptiveStatistics();
        values.forEach(statistics::addValue);

        final double min = statistics.getMin();
        final double max = statistics.getMax();
        final double iqr = statistics.getPercentile(75) - statistics.getPercentile(25);
        final double stdDev = statistics.getStandardDeviation();

        final int numBins;
        double binWidth;
        if (iqr > 0) {
            // Freedman-Diaconis rule for calculating the binWidth
            binWidth = (2 * iqr) / Math.cbrt(values.size());
            numBins = (int) Math.round((max - min) / binWidth);
        }
        else if (max - min > 0) {
            // fall back to Sturges rule, if the binWidth would become 0
            // Sturges Rule
            numBins = (int) (1 + Math.log(values.size()) / Math.log(2));
            binWidth = (max - min) / numBins;
        }
        else {
            // iqr == 0 and min == max -> all datapoints are the same -> use just a single bin
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

        final var labelFormat = new DecimalFormat("#.##");
        final String[] binLabels = new String[numBins];
        for (int i = 0; i < numBins; i++) {
            double left = min + i * binWidth;
            double right = min + (i + 1) * binWidth;
            binLabels[i] = "%s - %s".formatted(labelFormat.format(left), labelFormat.format(right));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", histogramData);
        result.put("labels", binLabels);
        return toJson(result);
    }

    /**
     * Get a scatterplot for two metrics.
     *
     * @param metricId
     *         the id of the first metric to include
     * @param secondMetricId
     *         the id of the second metric to include
     *
     * @return the contens of the scatter plot as JSON
     */
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
        var facade = new JacksonFacade();
        return facade.toJson(object);
    }

    /**
     * Returns the {@link ClassDetailsView} for the selected class.
     *
     * @param link
     *         the name of the class to show details for
     * @param request
     *         Stapler request
     * @param response
     *         Stapler response
     *
     * @return the new class details view
     */
    @SuppressWarnings("unused") // Called by jelly view
    public Object getDynamic(final String link, final StaplerRequest request, final StaplerResponse response) {
        return new ClassDetailsView(owner, link);
    }

    /**
     * Get the table model for the metrics details table.
     *
     * @param id
     *         the id of the table to retrieve
     *
     * @return the {@link MetricsTableModel} containing all metrics
     */
    @Override
    public TableModel getTableModel(final String id) {
        return new MetricsTableModel(id, supportedMetrics, metricsMeasurements);
    }

    /**
     * Data class for points in a scatter plot.
     */
    private static final class ScatterPlotDataItem {
        private final String name;
        private final List<Number> value;

        /**
         * Create a new data point with a name and a list of values. The first two values will be the coordinates of the
         * item.
         *
         * @param name
         *         the name for the data point
         * @param values
         *         the values for the data point
         */
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
