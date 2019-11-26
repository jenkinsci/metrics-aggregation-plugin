package io.jenkins.plugins.metrics.analysis;

import org.junit.jupiter.api.Test;

import shaded.net.sourceforge.pmd.lang.ast.Node;

import io.jenkins.plugins.metrics.analysis.PMDMetricRule;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PMDMetricRuleTest {

    @Test
    public void shouldUseJavaAndTypeResolution() {
        PMDMetricRule rule = new PMDMetricRule();

        assertThat(rule.isTypeResolution()).isTrue();
        assertThat(rule.getLanguage().getName()).isEqualToIgnoringCase("java");
    }

    @Test
    public void shouldGetAllDescendantNodes() {
        PMDMetricRule rule = new PMDMetricRule();

        Node node = mock(Node.class);
        rule.streamAllDescendantNodes(node);

        verify(node, times(1)).findDescendantsOfType(eq(Node.class), any(), eq(true));
        verify(node, never()).findDescendantsOfType(any(), any(), eq(false));
    }
}
