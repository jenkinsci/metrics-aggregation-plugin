package io.jenkins.plugins.metrics.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import shaded.net.sourceforge.pmd.RuleContext;
import shaded.net.sourceforge.pmd.lang.LanguageRegistry;
import shaded.net.sourceforge.pmd.lang.ast.Node;
import shaded.net.sourceforge.pmd.lang.java.JavaLanguageModule;
import shaded.net.sourceforge.pmd.lang.metrics.LanguageMetricsProvider;
import shaded.net.sourceforge.pmd.lang.rule.AbstractRule;

public class PMDMetricRule extends AbstractRule {

    /**
     * Create a new PMD rule for reporting Java metrics.
     */
    public PMDMetricRule() {
        super.setLanguage(LanguageRegistry.getLanguage(JavaLanguageModule.NAME));
        // Enable Type Resolution on Java Rules by default
        super.setTypeResolution(true);
    }

    @Override
    public void apply(final List<? extends Node> nodes, final RuleContext ruleContext) {

        LanguageMetricsProvider<?, ?> languageMetricsProvider = ruleContext.getLanguageVersion()
                .getLanguageVersionHandler()
                .getLanguageMetricsProvider();

        nodes.stream()
                .flatMap(this::streamAllDescendantNodes)
                .forEach(node -> {
                    String violation = languageMetricsProvider.computeAllMetricsFor(node)
                            .entrySet().stream()
                            .map(entry -> entry.getKey().name() + "=" + entry.getValue())
                            .reduce("", (acc, entry) -> acc + entry + ",");
                    if (violation != null && !violation.isEmpty()) {
                        violation = node.getXPathNodeName() + "::" + violation;
                        addViolationWithMessage(ruleContext, node, violation);
                    }
                });
    }

    @VisibleForTesting
    Stream<Node> streamAllDescendantNodes(final Node node) {
        List<Node> descendants = new ArrayList<>();
        node.findDescendantsOfType(Node.class, descendants, true);
        return descendants.stream();
    }
}
