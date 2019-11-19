package io.jenkins.plugins.metrics.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.java.JavaLanguageModule;
import net.sourceforge.pmd.lang.metrics.LanguageMetricsProvider;
import net.sourceforge.pmd.lang.metrics.MetricKey;
import net.sourceforge.pmd.lang.rule.AbstractRule;

public class MetricRule extends AbstractRule {

    public MetricRule() {
        super.setLanguage(LanguageRegistry.getLanguage(JavaLanguageModule.NAME));
        // Enable Type Resolution on Java Rules by default
        super.setTypeResolution(true);
    }

    @Override
    public void apply(final List<? extends Node> nodes, final RuleContext ruleContext) {

        LanguageMetricsProvider<?, ?> pro = ruleContext.getLanguageVersion()
                .getLanguageVersionHandler()
                .getLanguageMetricsProvider();

        nodes.stream()
                .flatMap(n -> {
                    List<Node> descendants = new ArrayList<>();
                    n.findDescendantsOfType(Node.class, descendants, true);
                    return descendants.stream();
                })
                .forEach(node -> {
                    String violation = pro.computeAllMetricsFor(node)
                            .entrySet()
                            .stream()
                            .map(entry -> entry.getKey().name() + "=" + entry.getValue())
                            .reduce("", (acc, entry) -> acc + entry + ",");
                    if (violation != null && !violation.isEmpty()) {
                        addViolationWithMessage(ruleContext, node, violation);
                    }
                });
    }

    private Map<MetricKey<?>, Double> allMetrics(final LanguageMetricsProvider<?, ?> provider, final Node n) {
        // the map may have some NaN values, when the metric is not supported  
        return provider.computeAllMetricsFor(n)
                .entrySet()
                .stream()
                .filter(it -> !it.getValue().isNaN())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    }
}
