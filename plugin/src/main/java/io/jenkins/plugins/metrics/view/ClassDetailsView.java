package io.jenkins.plugins.metrics.view;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.kohsuke.stapler.bind.JavaScriptMethod;
import org.kohsuke.stapler.export.ExportedBean;
import hudson.model.ModelObject;
import hudson.model.Run;

import io.jenkins.plugins.datatables.DefaultAsyncTableContentProvider;
import io.jenkins.plugins.datatables.TableModel;
import io.jenkins.plugins.metrics.extension.MetricsProviderFactory;
import io.jenkins.plugins.metrics.model.MetricsProvider;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.Metric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;
import io.jenkins.plugins.metrics.util.JacksonFacade;

/**
 * Detail view for displaying metrics information for a specific class.
 *
 * @author Andreas Pabst
 */
@ExportedBean
public class ClassDetailsView extends DefaultAsyncTableContentProvider implements ModelObject {
    private final Run<?, ?> owner;
    private final ClassMetricsMeasurement metricsMeasurement;
    private final List<MetricDefinition> supportedMetrics;
    private final Map<String, String> classOverview;
    private final Map<String, Double> metricsMaxima;

    public ClassDetailsView(final Run<?, ?> owner, final String className) {
        this.owner = owner;

        metricsMeasurement = (ClassMetricsMeasurement) MetricsProviderFactory.getAllFor(owner.getAllActions()).stream()
                .map(MetricsProvider::getMetricsMeasurements)
                .flatMap(List::stream)
                .filter(m -> m.getQualifiedClassName().equals(className))
                .filter(Objects::nonNull)
                .reduce(MetricsMeasurement::merge)
                .orElse(new ClassMetricsMeasurement());

        supportedMetrics = MetricsProviderFactory.getAllSupportedMetricsFor(owner.getAllActions())
                .stream()
                .filter(metricDefinition -> metricDefinition.validForScope(Scope.METHOD))
                .collect(Collectors.toList());

        metricsMaxima = supportedMetrics.stream()
                .collect(Collectors.toMap(MetricDefinition::getId, metric -> metricsMeasurement.getChildren().stream()
                        .map(metricsMeasurement -> metricsMeasurement.getMetric(metric.getId()))
                        .map(d -> d.orElse(0.0).doubleValue())
                        .filter(Double::isFinite)
                        .max(Double::compare)
                        .orElse(0.0))
                );

        classOverview = metricsMeasurement.getMetrics()
                .values().stream()
                .collect(Collectors.toMap(metric -> metric.getMetricDefinition().getDisplayName(),
                        Metric::renderValue));
    }

    @Override
    public String getDisplayName() {
        return Messages.metrics_for(metricsMeasurement.getClassName());
    }

    public String getPackageName() {
        return metricsMeasurement.getPackageName();
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
    public Map<String, String> getClassOverview() {
        return classOverview;
    }

    @SuppressWarnings("unused") // used by jelly view
    public String getMetricsMaximaJSON() {
        return toJson(metricsMaxima);
    }

    @JavaScriptMethod
    @SuppressWarnings("unused") // used by jelly view
    public String getMetricsJSON() {
        return toJson(metricsMeasurement.getChildren());
    }

    private String toJson(final Object object) {
        JacksonFacade facade = new JacksonFacade();
        return facade.toJson(object);
    }

    @Override
    public TableModel getTableModel(final String id) {
        return new ClassDetailsTableModel(supportedMetrics, metricsMeasurement.getChildren());
    }
}
