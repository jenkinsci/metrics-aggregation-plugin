package io.jenkins.plugins.metrics.analysis;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;

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

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import io.jenkins.plugins.metrics.model.ClassMetricsMeasurement;
import io.jenkins.plugins.metrics.model.MethodMetricsMeasurement;
import io.jenkins.plugins.metrics.model.Metric;
import io.jenkins.plugins.metrics.model.MetricsMeasurement;
import io.jenkins.plugins.metrics.model.MetricsReport;
import io.jenkins.plugins.metrics.util.FileFinder;

public class MetricsActor extends MasterToSlaveFileCallable<MetricsReport> {
    private static final long serialVersionUID = 2843497011946621955L;

    private final String filePattern;

    public MetricsActor(final String filePattern) {
        super();
        this.filePattern = filePattern;
    }

    @Override
    public MetricsReport invoke(final File workspace, final VirtualChannel channel) {
        MetricsReport metricsReport = new MetricsReport();

        PMDConfiguration configuration = new PMDConfiguration();
        configuration.setDebug(true);
        configuration.setIgnoreIncrementalAnalysis(true);
        configuration.setRuleSets("io/jenkins/plugins/metrics/metricsRuleset.xml");

        RuleContext ruleContext = new RuleContext();

        FileFinder fileFinder = new FileFinder(filePattern);
        String[] srcFiles = fileFinder.find(workspace);
        metricsReport.logInfo("Analyzing %d files matching the pattern %s in %s", srcFiles.length, filePattern,
                workspace);

        Path workspaceRoot = workspace.toPath();
        configuration.setInputPaths(workspaceRoot.toString());

        List<DataSource> files = new LinkedList<>();
        for (String fileName : srcFiles) {
            File file = workspaceRoot.resolve(fileName).toFile();
            files.add(new FileDataSource(file));
        }

        RuleSetFactory ruleSetFactory = RulesetsFactoryUtils.getRulesetFactory(configuration,
                new ResourceLoader(getClass().getClassLoader()));

        Field[] opcodesFields = Opcodes.class.getDeclaredFields();
        String asmFields = Arrays.stream(opcodesFields)
                .map(Field::getName)
                .filter(f -> f.startsWith("ASM"))
                .collect(Collectors.joining(","));
        metricsReport.logInfo("Opcodes starting with asm: %s", asmFields);

        Field[] shadedOpcodesFields = shaded.org.objectweb.asm.Opcodes.class.getDeclaredFields();
        String shadedAsmFields = Arrays.stream(shadedOpcodesFields)
                .map(Field::getName)
                .filter(f -> f.startsWith("ASM"))
                .collect(Collectors.joining(","));
        metricsReport.logInfo("shaded Opcodes starting with asm: %s", shadedAsmFields);

        metricsReport.logInfo("pmd version: %s", PMD.VERSION);

        PMD.processFiles(configuration, ruleSetFactory, files, ruleContext,
                Collections.singletonList(new MetricsLogRenderer(metricsReport)));

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

        private final MetricsReport metricsReport;

        MetricsLogRenderer(final MetricsReport metricsReport) {
            super("log-metrics", "Metrics logging renderer");
            this.metricsReport = metricsReport;
        }

        @Override
        public void renderFileReport(final Report report) throws IOException {
            for (final RuleViolation ruleViolation : report) {
                MetricsMeasurement metricsMeasurement;
                ClassMetricsMeasurement classMetricsMeasurement = new ClassMetricsMeasurement();
                classMetricsMeasurement.setFileName(ruleViolation.getFilename());
                classMetricsMeasurement.setPackageName(ruleViolation.getPackageName());
                classMetricsMeasurement.setClassName(ruleViolation.getClassName());

                if (ruleViolation.getMethodName().isEmpty()) {
                    metricsMeasurement = classMetricsMeasurement;
                }
                else {
                    MethodMetricsMeasurement methodMetricsMeasurement = new MethodMetricsMeasurement();
                    methodMetricsMeasurement.setParent(classMetricsMeasurement);
                    methodMetricsMeasurement.setBeginLine(ruleViolation.getBeginLine());
                    methodMetricsMeasurement.setBeginColumn(ruleViolation.getBeginColumn());
                    methodMetricsMeasurement.setEndLine(ruleViolation.getEndLine());
                    methodMetricsMeasurement.setEndColumn(ruleViolation.getEndLine());
                    methodMetricsMeasurement.setMethodName(ruleViolation.getMethodName());
                    //methodMetricsMeasurement.setVariableName(ruleViolation.getVariableName());
                    metricsMeasurement = methodMetricsMeasurement;
                }

                String[] metrics = ruleViolation.getDescription().split(",");
                for (String metric : metrics) {
                    String[] keyValue = metric.split("=");
                    metricsMeasurement.addMetric(new Metric(keyValue[0],
                            keyValue[0].toLowerCase(),
                            "",
                            Double.parseDouble(keyValue[1])));

                }

                metricsReport.add(metricsMeasurement);
            }

            for (Iterator<ProcessingError> i = report.errors(); i.hasNext(); ) {
                metricsReport.logError("Error: %s", i.next().getDetail());
            }

            for (Iterator<ConfigurationError> i = report.configErrors(); i.hasNext(); ) {
                ConfigurationError error = i.next();
                metricsReport.logError("Configuration error in rule %s: %s", error.rule(), error.issue());
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
