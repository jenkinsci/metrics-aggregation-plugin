package io.jenkins.plugins.metrics.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import hudson.Extension;

import io.jenkins.plugins.metrics.analysis.MetricsAction;
import io.jenkins.plugins.metrics.model.MetricsProvider;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition.Scope;

/**
 * {@link MetricsProviderFactory} for PMD metrics.
 */
@Extension
public class PMDMetricsProviderFactory extends MetricsProviderFactory<MetricsAction> {

    private static final MetricDefinition CLASSES = new MetricDefinition("CLASSES",
            "Classes",
            "Number of classes.",
            "metrics-aggregation-plugin (pmd)",
            100,
            ArrayUtils.toArray());
    private static final MetricDefinition ATFD = new MetricDefinition("ATFD",
            "Access to Foreign Data",
            "Number of usages of foreign attributes, both directly and through accessors.",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition CLASS_FAN_OUT = new MetricDefinition("CLASS_FAN_OUT",
            "Class Fan Out Complexity",
            "Counts the number of other classes a given class or operation relies on.",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition CYCLO = new MetricDefinition("CYCLO",
            "Cyclomatic Complexity",
            "Number of independent paths through a block of code. Formally, given that the control flow graph "
                    + "of the block has n vertices, e edges and p connected components, the cyclomatic complexity of "
                    + "the block is given by CYCLO = e - n + 2p. In practice it can be calculated by counting control "
                    + "flow statements following the standard rules given below.",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.METHOD));
    private static final MetricDefinition LOC = new MetricDefinition("LOC",
            "Lines of Code",
            "Simply counts the number of lines of code the operation or class takes up in the source.",
            "metrics-aggregation-plugin (pmd)",
            5,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition NCSS = new MetricDefinition("NCSS",
            "Non-commenting source statements",
            "Number of statements in a class or operation. That’s roughly equivalent to counting the number "
                    + "of semicolons and opening braces in the program. Comments and blank lines are ignored, and "
                    + "statements spread on multiple lines count as only one "
                    + "(e.g. int\\n a; counts a single statement).",
            "metrics-aggregation-plugin (pmd)",
            5,
            ArrayUtils.toArray(Scope.METHOD, Scope.CLASS));
    private static final MetricDefinition NOAM = new MetricDefinition("NOAM",
            "Number of Accessor Methods",
            "",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.CLASS));
    private static final MetricDefinition NOPA = new MetricDefinition("NOPA",
            "Number of Public Attributes",
            "",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.CLASS));
    private static final MetricDefinition NPATH = new MetricDefinition("NPATH",
            "NPath complexity",
            "Number of acyclic execution paths through a piece of code. This is related to cyclomatic "
                    + "complexity, but the two metrics don’t count the same thing: NPath counts the number of distinct "
                    + "full paths from the beginning to the end of the method, while Cyclo only counts the number of "
                    + "decision points. NPath is not computed as simply as Cyclo. With NPath, two decision points "
                    + "appearing sequentially have their complexity multiplied.",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.METHOD));
    private static final MetricDefinition TCC = new MetricDefinition("TCC",
            "Tight Class Cohesion",
            "The relative number of method pairs of a class that access in common at least one attribute "
                    + "of the measured class.",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.CLASS));
    private static final MetricDefinition WMC = new MetricDefinition("WMC",
            "Weighted Method Count",
            "Sum of the statistical complexity of the operations in the class. We use CYCLO to quantify "
                    + "the complexity of an operation.",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.CLASS));
    private static final MetricDefinition WOC = new MetricDefinition("WOC",
            "Weight of Class",
            "Number of “functional” public methods divided by the total number of public methods. "
                    + "Our definition of “functional method” excludes constructors, getters, and setters.",
            "metrics-aggregation-plugin (pmd)",
            20,
            ArrayUtils.toArray(Scope.CLASS));

    @Override
    public Class<MetricsAction> type() {
        return MetricsAction.class;
    }

    @Override
    public MetricsProvider getFor(final List<MetricsAction> actions) {
        MetricsProvider provider = new MetricsProvider();
        provider.setOrigin("metrics-aggregation-plugin (pmd)");

        provider.setMetricsMeasurements(actions.stream()
                .map(MetricsAction::getMetricsMeasurements)
                .flatMap(List::stream)
                .collect(Collectors.toList())
        );

        long numberOfClasses = actions.stream()
                .flatMap(m -> m.getMetricsMeasurements().stream())
                .filter(metricsMeasurement -> metricsMeasurement instanceof ClassMetricsMeasurement)
                .count();

        if (numberOfClasses > 0) {
            provider.addProjectSummaryEntry(String.format("%d Classes", numberOfClasses));
            provider.addProjectMetric(new IntegerMetric(CLASSES, (int) numberOfClasses));
        }

        double totalLOC = getMetricSum(actions, LOC.getId());
        if (totalLOC > 0) {
            provider.addProjectSummaryEntry(String.format("%d Lines of Code", (long) totalLOC));
            provider.addProjectMetric(new IntegerMetric(LOC, (int) totalLOC));
        }
        double totalNCSS = getMetricSum(actions, NCSS.getId());
        if (totalNCSS > 0) {
            provider.addProjectSummaryEntry(
                    String.format("%d Non-Commenting Source Statements (%.1f%%)", (long) totalNCSS,
                            totalNCSS / totalLOC * 100));
            provider.addProjectMetric(new IntegerMetric(NCSS, (int) totalNCSS));
        }

        return provider;
    }

    private double getMetricSum(final List<MetricsAction> actions, final String metricId) {
        return actions.stream()
                .flatMap(m -> m.getMetricsMeasurements().stream())
                .filter(metricsMeasurement -> metricsMeasurement instanceof ClassMetricsMeasurement)
                .map(m -> m.getMetric(metricId).orElse(0.0).doubleValue())
                .reduce(0.0, Double::sum);
    }

    @Override
    public ArrayList<MetricDefinition> supportedMetricsFor(final List<MetricsAction> actions) {
        if (actions.isEmpty()) {
            return new ArrayList<>();
        }

        return getSupportedMetrics();
    }

    /**
     * Return all metrics supported by the metrics-aggregation plugin, i.e. by PMD.
     *
     * @return a list of supported {@link MetricDefinition}s
     */
    public static ArrayList<MetricDefinition> getSupportedMetrics() {
        return new ArrayList<>(Arrays.asList(ATFD, CLASS_FAN_OUT, CYCLO, LOC, NCSS, NPATH, NOAM, NOPA, TCC, WMC, WOC));
    }
}
