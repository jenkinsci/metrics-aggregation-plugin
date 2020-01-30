package io.jenkins.plugins.metrics.analysis;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import shaded.net.sourceforge.pmd.PMD;
import shaded.net.sourceforge.pmd.PMDConfiguration;
import shaded.net.sourceforge.pmd.Report;
import shaded.net.sourceforge.pmd.Report.ConfigurationError;
import shaded.net.sourceforge.pmd.Report.ProcessingError;
import shaded.net.sourceforge.pmd.RuleContext;
import shaded.net.sourceforge.pmd.RuleSetFactory;
import shaded.net.sourceforge.pmd.RuleViolation;
import shaded.net.sourceforge.pmd.RulesetsFactoryUtils;
import shaded.net.sourceforge.pmd.renderers.AbstractRenderer;
import shaded.net.sourceforge.pmd.util.ResourceLoader;
import shaded.net.sourceforge.pmd.util.datasource.DataSource;
import shaded.net.sourceforge.pmd.util.datasource.FileDataSource;

import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import io.jenkins.plugins.metrics.extension.PMDMetricsProviderFactory;
import io.jenkins.plugins.metrics.model.measurement.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MethodMetricsMeasurement;
import io.jenkins.plugins.metrics.model.measurement.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.metric.DoubleMetric;
import io.jenkins.plugins.metrics.model.metric.IntegerMetric;
import io.jenkins.plugins.metrics.model.metric.MetricDefinition;
import io.jenkins.plugins.metrics.util.FileFinder;

public class MetricsActor extends MasterToSlaveFileCallable<List<MetricsMeasurement>> {
    private static final long serialVersionUID = 2843497011946621955L;

    private final String filePattern;
    private final TaskListener listener;

    public MetricsActor(final String filePattern, final TaskListener listener) {
        super();
        this.filePattern = filePattern;
        this.listener = listener;
    }

    @Override
    public List<MetricsMeasurement> invoke(final File workspace, final VirtualChannel channel) {
        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setDebug(true);
        configuration.setIgnoreIncrementalAnalysis(true);
        configuration.setRuleSets("io/jenkins/plugins/metrics/metricsRuleset.xml");

        RuleContext ruleContext = new RuleContext();

        FileFinder fileFinder = new FileFinder(filePattern);
        String[] srcFiles = fileFinder.find(workspace);
        listener.getLogger()
                .printf("[Metrics] Analyzing %d files matching the pattern '%s' in %s%n",
                        srcFiles.length, filePattern, workspace);

        Path workspaceRoot = workspace.toPath();
        configuration.setInputPaths(workspaceRoot.toString());

        List<DataSource> files = new LinkedList<>();
        for (String fileName : srcFiles) {
            File file = workspaceRoot.resolve(fileName).toFile();
            files.add(new FileDataSource(file));
        }

        RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration,
                new ResourceLoader(getClass().getClassLoader()));

        List<MetricsMeasurement> metricsReport = new ArrayList<>();
        PMD.processFiles(configuration, ruleSetFactory, files, ruleContext,
                Collections.singletonList(new MetricsLogRenderer(metricsReport, listener)));

        return metricsReport;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof MetricsActor)) {
            return false;
        }

        MetricsActor other = (MetricsActor) o;
        if (this == other) {
            return true;
        }

        return Objects.equals(filePattern, other.filePattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePattern);
    }

    private static class MetricsLogRenderer extends AbstractRenderer {

        private final List<MetricsMeasurement> metricsReport;
        private final TaskListener listener;
        private final Map<String, MetricDefinition> supportedMetrics;

        MetricsLogRenderer(final List<MetricsMeasurement> metricsReport, final TaskListener listener) {
            super("log-metrics", "Metrics logging renderer");
            this.metricsReport = metricsReport;
            this.listener = listener;
            supportedMetrics = PMDMetricsProviderFactory.getSupportedMetrics()
                    .stream().collect(Collectors.toMap(MetricDefinition::getId, Function.identity()));
        }

        @Override
        public void renderFileReport(final Report report) {
            for (final RuleViolation ruleViolation : report) {
                final MetricsMeasurement metricsMeasurement;

                String description = ruleViolation.getDescription();
                if (description.startsWith("ClassOrInterfaceDeclaration::")) {
                    metricsMeasurement = new ClassMetricsMeasurement();
                }
                else {
                    metricsMeasurement = new MethodMetricsMeasurement();
                    ((MethodMetricsMeasurement) metricsMeasurement).setMethodName(ruleViolation.getMethodName());
                    ((MethodMetricsMeasurement) metricsMeasurement).setBeginLine(ruleViolation.getBeginLine());
                    ((MethodMetricsMeasurement) metricsMeasurement).setBeginColumn(ruleViolation.getBeginColumn());
                    ((MethodMetricsMeasurement) metricsMeasurement).setEndLine(ruleViolation.getEndLine());
                    ((MethodMetricsMeasurement) metricsMeasurement).setEndColumn(ruleViolation.getEndLine());
                }

                (metricsMeasurement).setFileName(ruleViolation.getFilename());
                (metricsMeasurement).setPackageName(ruleViolation.getPackageName());
                (metricsMeasurement).setClassName(ruleViolation.getClassName());

                description = description.replaceFirst(".*::", "");

                String[] metrics = description.split(",");
                for (String metric : metrics) {
                    String[] keyValue = metric.split("=");
                    String metricName = keyValue[0];
                    double metricValue = Double.parseDouble(keyValue[1]);

                    MetricDefinition metricDefinition = supportedMetrics.get(metricName);

                    if (metricDefinition == null) {
                        listener.getLogger().printf("Ignoring unknown PMD metric: %s%n", keyValue[0]);
                    }
                    else {
                        switch (metricName) {
                            case "ATFD":
                            case "CLASS_FAN_OUT":
                            case "NCSS":
                            case "LOC":
                            case "NOAM":
                            case "NPATH":
                            case "CYCLO":
                            case "NOPA":
                                metricsMeasurement.addMetric(new IntegerMetric(metricDefinition, (int) metricValue));
                                break;
                            case "TCC":
                            case "WMC":
                            case "WOC":
                                metricsMeasurement.addMetric(new DoubleMetric(metricDefinition, metricValue));
                                break;
                            default:
                                listener.getLogger().printf("Ignoring unknown PMD metric: %s%n", keyValue[0]);
                        }
                    }
                }

                metricsReport.add(metricsMeasurement);
            }

            for (Iterator<ProcessingError> i = report.errors(); i.hasNext(); ) {
                ProcessingError error = i.next();
                listener.error("ProcessingError in File '%s':%n%s", error.getFile(), error.getDetail());
            }

            for (Iterator<ConfigurationError> i = report.configErrors(); i.hasNext(); ) {
                ConfigurationError error = i.next();
                listener.error("Configuration error in rule %s: %s", error.rule(), error.issue());
            }
        }

        @Override
        public String defaultFileExtension() {
            return null;
        }

        @Override
        public void start() {
        }

        @Override
        public void startFileAnalysis(final DataSource dataSource) {
        }

        @Override
        public void end() {
        }

    }
}
