package io.jenkins.plugins.metrics.model;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.MetricDefinitionBuilder;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

import static io.jenkins.plugins.metrics.assertions.Assertions.*;

/**
 * Test for the class {@link MetricDefinition}.
 */
public class MetricDefinitionTest {
    /**
     * Test if the scope check is working.
     */
    @Test
    public void shouldBeValidForScope() {
        var both = createMetricDefinitionWithScope(Scope.CLASS, Scope.METHOD);
        assertThat(both.isValidForScope(Scope.CLASS)).isTrue();
        assertThat(both.isValidForScope(Scope.METHOD)).isTrue();

        var justClass = createMetricDefinitionWithScope(Scope.CLASS);
        assertThat(justClass.isValidForScope(Scope.CLASS)).isTrue();
        assertThat(justClass.isValidForScope(Scope.METHOD)).isFalse();

        var justMethod = createMetricDefinitionWithScope(Scope.METHOD);
        assertThat(justMethod.isValidForScope(Scope.METHOD)).isTrue();
        assertThat(justMethod.isValidForScope(Scope.CLASS)).isFalse();
    }

    @Test
    void shouldCreateMetricDefinition() {
        var first = new MetricDefinitionBuilder("First")
                .withDisplayName("First Metric")
                .withDescription("First Description")
                .withReportedBy("First Reported By")
                .withPriority(1)
                .withScopes(Scope.CLASS)
                .build();
        var second = new MetricDefinitionBuilder("Second")
                .withDisplayName("Second Metric")
                .withDescription("Second Description")
                .withReportedBy("Second Reported By")
                .withPriority(2)
                .withScopes(Scope.METHOD)
                .build();

        assertThat(first.compareTo(second)).isNegative();
        assertThat(second.compareTo(first)).isPositive();

        assertThat(first).hasDisplayName("First Metric")
                .hasDescription("First Description")
                .hasReportedBy("First Reported By")
                .hasPriority(1)
                .hasScopes(Scope.CLASS)
                .hasId("First");
        assertThat(second).hasDisplayName("Second Metric")
                .hasDescription("Second Description")
                .hasReportedBy("Second Reported By")
                .hasPriority(2)
                .hasScopes(Scope.METHOD)
                .hasId("Second");
    }

    private MetricDefinition createMetricDefinitionWithScope(final Scope... scopes) {
        return new MetricDefinitionBuilder("ID").withScopes(scopes).build();
    }

    @Test
    void shouldAdhereToEquals() {
        EqualsVerifier.forClass(MetricDefinition.class).withOnlyTheseFields("id").verify();
    }
}
