package io.jenkins.plugins.metrics.model;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

import static org.assertj.core.api.Assertions.*;

public class MetricDefinitionTest {

    @Test
    public void shouldBeValidForScope() {
        final MetricDefinition both = createMetricDefinitionWithScope(ArrayUtils.toArray(Scope.CLASS, Scope.METHOD));
        assertThat(both.validForScope(Scope.CLASS)).isTrue();
        assertThat(both.validForScope(Scope.METHOD)).isTrue();

        final MetricDefinition justClass = createMetricDefinitionWithScope(ArrayUtils.toArray(Scope.CLASS));
        assertThat(justClass.validForScope(Scope.CLASS)).isTrue();
        assertThat(justClass.validForScope(Scope.METHOD)).isFalse();

        final MetricDefinition justMethod = createMetricDefinitionWithScope(ArrayUtils.toArray(Scope.METHOD));
        assertThat(justMethod.validForScope(Scope.METHOD)).isTrue();
        assertThat(justMethod.validForScope(Scope.CLASS)).isFalse();
    }

    public MetricDefinition createMetricDefinitionWithScope(final Scope[] scopes) {
        return new MetricDefinition("", "", "", "", 0, scopes);
    }
}
