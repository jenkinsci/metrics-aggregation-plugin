package io.jenkins.plugins.metrics.analysis;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.annotations.VisibleForTesting;

import shaded.net.sourceforge.pmd.RuleContext;
import shaded.net.sourceforge.pmd.lang.ast.Node;
import shaded.net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration;
import shaded.net.sourceforge.pmd.lang.java.ast.ASTFormalParameter;
import shaded.net.sourceforge.pmd.lang.java.ast.ASTFormalParameters;
import shaded.net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration;
import shaded.net.sourceforge.pmd.lang.java.ast.ASTResultType;
import shaded.net.sourceforge.pmd.lang.java.ast.ASTType;
import shaded.net.sourceforge.pmd.lang.java.rule.AbstractJavaMetricsRule;
import shaded.net.sourceforge.pmd.lang.metrics.LanguageMetricsProvider;
import shaded.net.sourceforge.pmd.lang.metrics.MetricKey;

public class PMDMetricRule extends AbstractJavaMetricsRule {

    @Override
    public void apply(final List<? extends Node> nodes, final RuleContext ruleContext) {
        LanguageMetricsProvider<?, ?> languageMetricsProvider = ruleContext.getLanguageVersion()
                .getLanguageVersionHandler()
                .getLanguageMetricsProvider();

        nodes.stream()
                .flatMap(this::descendantNodes)
                .forEach(node -> {
                    String violation = getMetricsAsMessageString(languageMetricsProvider.computeAllMetricsFor(node));

                    if (violation.trim().isEmpty()) {
                        return;
                    }

                    if (node instanceof ASTMethodDeclaration) {
                        final String parameters = formalParameterClasses(
                                ((ASTMethodDeclaration) node).getFormalParameters());

                        String methodReturnType = "void";
                        ASTResultType result = ((ASTMethodDeclaration) node).getResultType();
                        if (result != null && !result.isVoid()) {
                            ASTType type = result.getFirstChildOfType(ASTType.class);
                            methodReturnType = classOfType(type);
                        }

                        violation = String.format("%s (%s)::", methodReturnType, parameters) + violation;
                    }
                    else if (node instanceof ASTConstructorDeclaration) {
                        final String parameters = formalParameterClasses(
                                ((ASTConstructorDeclaration) node).getFormalParameters());

                        violation = String.format("void (%s)::", parameters) + violation;
                    }

                    // prepend the type of the node
                    violation = node.getXPathNodeName() + "::" + violation;
                    addViolationWithMessage(ruleContext, node, violation);
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

    private String classOfType(final ASTType type) {
        if (type != null && type.getType() != null) {
            return type.getType().getName();
        }
        else if (type != null) {
            return type.getTypeImage();
        }
        else {
            return "";
        }
    }

    @VisibleForTesting
    String getClassOfFormalParameter(final ASTFormalParameter parameter) {
        if (parameter != null && parameter.getType() != null) {
            String type = parameter.getType().getName();
            if (parameter.getTypeDefinition().isArrayType()) {
                type = type.replaceFirst("\\[L", "");
                type = type.replaceFirst(";", "[]");
            }
            return type;
        }
        else {
            return "";
        }
    }

    private String formalParameterClasses(final ASTFormalParameters parameters) {
        return parameters.findChildrenOfType(ASTFormalParameter.class)
                .stream()
                .map(this::getClassOfFormalParameter)
                .collect(Collectors.joining(","));
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
        return node.findDescendantsOfType(Node.class, true).stream();
    }
}
