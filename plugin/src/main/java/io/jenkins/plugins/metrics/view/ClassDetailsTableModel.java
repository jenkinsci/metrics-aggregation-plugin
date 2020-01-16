package io.jenkins.plugins.metrics.view;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.jenkins.plugins.datatables.TableColumn;
import io.jenkins.plugins.datatables.TableConfiguration;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;

public class ClassDetailsTableModel extends TableModel {

    private final List<MetricDefinition> supportedMetrics;
    private final List<MetricsMeasurement> metricsMeasurements;

    public ClassDetailsTableModel(final List<MetricDefinition> supportedMetrics,
            final List<MetricsMeasurement> metricsMeasurements) {
        this.supportedMetrics = supportedMetrics;
        this.metricsMeasurements = metricsMeasurements;
    }

    @Override
    public String getId() {
        return "metrics-table";
    }

    @Override
    public List<TableColumn> getColumns() {
        List<TableColumn> columns = new ArrayList<>();
        columns.add(new TableColumn("Line", "beginLine"));
        columns.add(new TableColumn("Method", "methodName"));

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
        return new ArrayList<>(metricsMeasurements);
    }

    @Override
    public TableConfiguration getTableConfiguration() {
        return new TableConfiguration()
                .responsive()
                .colReorder()
                .stateSave()
                .buttons("colvis");
    }
}
