package io.jenkins.plugins.metrics.model;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
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
        var first = new MetricDefinition("First", "First Metric", "First Description",
                "First Reported By", 1, Scope.CLASS);
        var second = new MetricDefinition("Second", "Second Metric", "Second Description",
                "Second Reported By", 2, Scope.METHOD);

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
        return new MetricDefinition("", "", "", "", 0, scopes);
    }

    @Test
    void shouldAdhereToEquals() {
        EqualsVerifier.forClass(MetricDefinition.class).withOnlyTheseFields("id").verify();
    }
}
