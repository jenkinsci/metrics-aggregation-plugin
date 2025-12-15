package io.jenkins.plugins.metrics.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableConfiguration;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricDefinition;

import static j2html.TagCreator.*;

/**
 * {@link TableModel} for all metrics in a project overview.
 */
@SuppressWarnings("deprecation")
public class MetricsTableModel extends TableModel {
    private final List<MetricDefinition> supportedMetrics;
    private final List<ClassMetricsMeasurement> metricsMeasurements;
    private final String id;

    /**
     * Create a new {@link MetricsTableModel}.
     *
     * @param id
     *         the id of the table
     * @param supportedMetrics
     *         the metrics supported for this table
     * @param metricsMeasurements
     *         the {@link ClassMetricsMeasurement}s to display in the table
     */
    public MetricsTableModel(final String id, final List<MetricDefinition> supportedMetrics,
            final List<ClassMetricsMeasurement> metricsMeasurements) {
        super();

        this.id = id;
        this.supportedMetrics = supportedMetrics;
        this.metricsMeasurements = metricsMeasurements;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn("Class", "className"));

        columns.addAll(supportedMetrics
                .stream()
                .map(metricDefinition -> new TableColumn(metricDefinition.getDisplayName(),
                        "metricsDisplay." + metricDefinition.getId()))
                .collect(Collectors.toList())
        );

        return columns;
    }

    @Override
    public List<Object> getRows() {
        return metricsMeasurements.stream()
                .map(MetricsRow::new)
                .collect(Collectors.toList());
    }

    @Override
    public TableConfiguration getTableConfiguration() {
        return new TableConfiguration()
                .responsive()
                .colReorder()
                .stateSave()
                .buttons("colvis");
    }

    /**
     * A row in the table in the metrics overview. Contains the name of the class plus all available metrics.
     */
    public static class MetricsRow {
        private final ClassMetricsMeasurement metricsMeasurement;

        MetricsRow(final ClassMetricsMeasurement metricsMeasurement) {
            this.metricsMeasurement = metricsMeasurement;
        }

        /**
         * Get the Name of a class. At the same time this is a link to the detail view (@link {@link ClassDetailsView})
         * for this class.
         *
         * @return a link to the details with the name of the class as text
         */
        public String getName() {
            return span()
                    .withTitle(metricsMeasurement.getPackageName())
                    .with(a()
                            .withHref(metricsMeasurement.getQualifiedClassName())
                            .withText(metricsMeasurement.getClassName())
                            .withTarget("blank")
                    )
                    .render();
        }

        /**
         * Get the metrics of this measurement as they should be displayed (i.e. formatted).
         *
         * @return the map of metrics
         */
        public Map<String, String> getMetricsDisplay() {
            return metricsMeasurement.getMetrics()
                    .values().stream()
                    .collect(Collectors.toMap(Metric::getId, Metric::renderValue));
        }

        /**
         * Get the raw metrics of this measurement e.g. for use in filtering.
         *
         * @return the map of metrics
         */
        public Map<String, Number> getMetricsRaw() {
            return metricsMeasurement.getMetrics()
                    .values().stream()
                    .collect(Collectors.toMap(Metric::getId, Metric::rawValue));
        }
    }
}
