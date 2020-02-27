package io.jenkins.plugins.metrics.analysis;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import shaded.net.sourceforge.pmd.lang.ast.Node;
import shaded.net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import shaded.net.sourceforge.pmd.lang.java.metrics.api.JavaClassMetricKey;
import shaded.net.sourceforge.pmd.lang.java.typeresolution.typedefinition.JavaTypeDefinition;
import shaded.net.sourceforge.pmd.lang.metrics.MetricKey;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test for the class {@link PMDMetricRule}.
 */
public class PMDMetricRuleTest {

    @Test
    void shouldUseJavaAndTypeResolution() {
        PMDMetricRule rule = new PMDMetricRule();

        assertThat(rule.isTypeResolution()).isTrue();
        assertThat(rule.getLanguage().getName()).isEqualToIgnoringCase("java");
    }

    @Test
    void shouldGetAllDescendantNodes() {
        PMDMetricRule rule = new PMDMetricRule();

        Node node = mock(Node.class);
        rule.descendantNodes(node);

        verify(node, times(1)).findDescendantsOfType(eq(Node.class), eq(true));
        verify(node, never()).findDescendantsOfType(any(), eq(false));
    }

    @Test
    void shouldConvertEmptyMetricsToEmptyString() {
        PMDMetricRule rule = new PMDMetricRule();
        Map<MetricKey<?>, Double> emptyMetrics = new HashMap<>();
        assertThat(rule.getMetricsAsMessageString(emptyMetrics)).isEqualTo("");
    }

    @Test
    void shouldConvertMetricsToString() {
        PMDMetricRule rule = new PMDMetricRule();

        Map<MetricKey<?>, Double> metrics = new HashMap<>();
        metrics.put(JavaClassMetricKey.LOC, 1.0);
        metrics.put(JavaClassMetricKey.NCSS, 2.0);
        metrics.put(JavaClassMetricKey.CLASS_FAN_OUT, 3.0);

        String[] message = rule.getMetricsAsMessageString(metrics).split(",");
        assertThat(message).hasSize(3);
        assertThat(message).containsExactlyInAnyOrder("LOC=1.0", "NCSS=2.0", "CLASS_FAN_OUT=3.0");
    }

    @Test
    void shouldGetClassOfFormalParameter() {
        PMDMetricRule rule = new PMDMetricRule();
        ASTFormalParameter param = mock(ASTFormalParameter.class);

        when(param.getType()).then(x -> String.class);
        when(param.getTypeDefinition()).thenReturn(JavaTypeDefinition.forClass(String.class));
        assertThat(rule.getClassOfFormalParameter(param)).isEqualTo("java.lang.String");

        when(param.getType()).then(x -> String[].class);
        when(param.getTypeDefinition()).thenReturn(JavaTypeDefinition.forClass(String[].class));
        assertThat(rule.getClassOfFormalParameter(param)).isEqualTo("java.lang.String[]");
    }
}
