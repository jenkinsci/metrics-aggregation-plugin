package io.jenkins.plugins.metrics.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import shaded.net.sourceforge.pmd.RuleContext;
import shaded.net.sourceforge.pmd.lang.LanguageRegistry;
import shaded.net.sourceforge.pmd.lang.ast.Node;
import shaded.net.sourceforge.pmd.lang.java.JavaLanguageModule;
import shaded.net.sourceforge.pmd.lang.metrics.LanguageMetricsProvider;
import shaded.net.sourceforge.pmd.lang.metrics.MetricKey;
import shaded.net.sourceforge.pmd.lang.rule.AbstractRule;

public class PMDMetricRule extends AbstractRule {

    /**
     * Create a new PMD rule for reporting all available Java metrics.
     */
    public PMDMetricRule() {
        super.setLanguage(LanguageRegistry.getLanguage(JavaLanguageModule.NAME));
        // Enable type resolution on Java rules by default
        super.setTypeResolution(true);
    }

    @Override
    public void apply(final List<? extends Node> nodes, final RuleContext ruleContext) {
        LanguageMetricsProvider<?, ?> languageMetricsProvider = ruleContext.getLanguageVersion()
                .getLanguageVersionHandler()
                .getLanguageMetricsProvider();

        nodes.stream()
                .flatMap(this::descendantNodes)
                .forEach(node -> {
                    String violation = getMetricsAsMessageString(languageMetricsProvider.computeAllMetricsFor(node));

                    if (!violation.isEmpty()) {
                        violation = node.getXPathNodeName() + "::" + violation;
                        addViolationWithMessage(ruleContext, node, violation);
                    }
                });
    }

    /**
     * Get all metrics as a string, to be used in a violation message. The message will have the format:
     * "metric1Name=metric1Value,metric2Name=metric2Value,"
     *
     * @param metrics
     *         the map of metrics, returned by {@link LanguageMetricsProvider#computeAllMetricsFor(Node)}
     *
     * @return the string message containing all metrics information
     */
    @VisibleForTesting
    String getMetricsAsMessageString(final Map<MetricKey<?>, Double> metrics) {
        return metrics
                .entrySet()
                .stream()
                .map(entry -> entry.getKey().name() + "=" + entry.getValue())
                .reduce("", (acc, entry) -> acc + entry + ",");
    }

    /**
     * Returns all the descendants of a specified {@link Node}.
     *
     * @param node
     *         the parent {@link Node} whose descendants are considered
     *
     * @return a stream of all descendant {@link Node}s
     */
    @VisibleForTesting
    Stream<Node> descendantNodes(final Node node) {
        final List<Node> descendants = new ArrayList<>();
        node.findDescendantsOfType(Node.class, descendants, true);
        return descendants.stream();
    }
}
