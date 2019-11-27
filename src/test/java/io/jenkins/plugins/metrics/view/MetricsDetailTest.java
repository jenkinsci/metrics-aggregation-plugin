package io.jenkins.plugins.metrics.view;

import org.junit.jupiter.api.Test;

import hudson.model.Run;

import io.jenkins.plugins.metrics.analysis.MetricsAction;
import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.MetricsReport;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class MetricsDetailTest {

    @Test
    void shouldReturnQuotedCSV() {
        MetricsDetail metricsDetail = new MetricsDetail(null);

        String result = metricsDetail.toCSV("hello", "world", "with,some,commas");

        assertThat(result).isEqualTo("\"hello\",\"world\",\"with,some,commas\"");
    }

    @Test
    public void shouldGetHistogram() {
        final String key = "key";
        MetricsReport measurements = new MetricsReport();
        measurements.add(getMeasurementWithMetric(key, 5.0));
        measurements.add(getMeasurementWithMetric(key, 2.0));
        measurements.add(getMeasurementWithMetric(key, 1.1));
        measurements.add(getMeasurementWithMetric(key, 10.0));
        measurements.add(getMeasurementWithMetric(key, 1.0));
        measurements.add(getMeasurementWithMetric(key, 17.0));

        measurements.add(getMeasurementWithMetric(key + "foo", 3.0));

        Run run = mock(Run.class);
        when(run.getAction(MetricsAction.class)).thenReturn(new MetricsAction(measurements));
        MetricsDetail metricsDetail = new MetricsDetail(run);

        String json = metricsDetail.getHistogram(key);
        assertThat(json).isEqualTo("{\"data\":[3,0,1,0,0,1,0,0,0,1],"
                + "\"labels\":[\"1,0 - 2,6\",\"2,6 - 4,2\",\"4,2 - 5,8\",\"5,8 - 7,4\",\"7,4 - 9,0\","
                + "\"9,0 - 10,6\",\"10,6 - 12,2\",\"12,2 - 13,8\",\"13,8 - 15,4\",\"15,4 - 17,0\"]}");
    }

    @Test
    public void shouldGetStatistics() {
        final String key = "key";

        MetricsReport measurements = new MetricsReport();
        measurements.add(getMeasurementWithMetric(key, 5.0));
        measurements.add(getMeasurementWithMetric(key, 2.0));
        measurements.add(getMeasurementWithMetric(key, 1.1));
        measurements.add(getMeasurementWithMetric(key, 10.0));
        measurements.add(getMeasurementWithMetric(key, 1.0));
        measurements.add(getMeasurementWithMetric(key, 17.0));

        Run run = mock(Run.class);
        when(run.getAction(MetricsAction.class)).thenReturn(new MetricsAction(measurements));
        MetricsDetail metricsDetail = new MetricsDetail(run);

        String json = metricsDetail.getStatistics(key);

    }

    private MetricsMeasurement getMeasurementWithMetric(final String key, final double value) {
        MetricsMeasurement metricsMeasurement = new ClassMetricsMeasurement();
        metricsMeasurement.addMetric(new Metric(key, key, key, value));
        return metricsMeasurement;
    }
}
