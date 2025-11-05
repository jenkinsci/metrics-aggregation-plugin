package io.jenkins.plugins.metrics.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import nl.jqno.equalsverifier.EqualsVerifier;

import io.jenkins.plugins.metrics.model.MetricDefinition.MetricDefinitionBuilder;
import io.jenkins.plugins.metrics.model.MetricDefinition.Scope;

import static io.jenkins.plugins.metrics.assertions.Assertions.*;

class MetricTest {
    private static final String ID = "ID";
    private static final MetricDefinition DEFINITION = createDefinition();

    private static MetricDefinition createDefinition() {
        return new MetricDefinitionBuilder(ID)
                .withDisplayName("Display Name")
                .withDescription("Description")
                .withReportedBy("Reported By")
                .withPriority(1)
                .withScopes(Scope.CLASS)
                .build();
    }

    @Test
    void shouldCreateDoubleMetric() {
        var metric = new DoubleMetric(DEFINITION, 3.14);

        assertThat(metric).hasId(ID).hasMetricDefinition(DEFINITION);
        assertThat(metric.rawValue()).isEqualTo(3.14);
        assertThat(metric.renderValue()).isEqualTo("3.14");
        assertThat(metric.needsRounding()).isFalse();
    }

    @Test
    void shouldCreateIntegerMetric() {
        var metric = new IntegerMetric(DEFINITION, 12);

        assertThat(metric).hasId(ID).hasMetricDefinition(DEFINITION);
        assertThat(metric.rawValue()).isEqualTo(12);
        assertThat(metric.renderValue()).isEqualTo("12");
        assertThat(metric.needsRounding()).isTrue();
    }

    @Test
    void shouldCreatePercentageMetric() {
        var metric = new PercentageMetric(DEFINITION, 99.99f);

        assertThat(metric).hasId(ID).hasMetricDefinition(DEFINITION);
        assertThat(metric.rawValue()).isEqualTo(99.99f);
        assertThat(metric.renderValue()).isEqualTo("100%");
        assertThat(metric.needsRounding()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(classes = {DoubleMetric.class, IntegerMetric.class, PercentageMetric.class})
    void shouldVerifyEqualsAndHashCode(final Class<?> metricClass) {
        EqualsVerifier.forClass(metricClass).withRedefinedSuperclass().verify();
    }
}
