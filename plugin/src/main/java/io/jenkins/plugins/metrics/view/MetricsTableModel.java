package io.jenkins.plugins.metrics.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableConfiguration;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;

import static j2html.TagCreator.*;

public class MetricsTableModel extends TableModel {

    private final List<MetricDefinition> supportedMetrics;
    private final List<ClassMetricsMeasurement> metricsMeasurements;
    private final String id;

    public MetricsTableModel(final String id, final List<MetricDefinition> supportedMetrics,
            final List<ClassMetricsMeasurement> metricsMeasurements) {
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
        public String getClassName() {
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
            return metricsMeasurement.getMetricsDisplay();
        }

        /**
         * Get the raw metrics of this measurement e.g. for use in filtering.
         *
         * @return the map of metrics
         */
        public Map<String, Number> getMetricsRaw() {
            return metricsMeasurement.getMetricsRaw();
        }
    }
}
