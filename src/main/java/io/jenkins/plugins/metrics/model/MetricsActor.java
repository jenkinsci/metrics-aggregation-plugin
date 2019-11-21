package io.jenkins.plugins.metrics.model;

import java.io.File;
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

import net.sourceforge.pmd.PMD;
import net.sourceforge.pmd.PMDConfiguration;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.Report.ProcessingError;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.RulesetsFactoryUtils;
import net.sourceforge.pmd.ThreadSafeReportListener;
import net.sourceforge.pmd.renderers.AbstractRenderer;
import net.sourceforge.pmd.stat.Metric;
import net.sourceforge.pmd.util.ResourceLoader;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.FileDataSource;

import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

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
        ruleContext.getReport().addListener(new ThreadSafeReportListener() {
            @Override
            public void ruleViolationAdded(RuleViolation ruleViolation) {

                MetricsMeasurement metricsMeasurement = new MetricsMeasurement();
                metricsMeasurement.setFileName(ruleViolation.getFilename());
                metricsMeasurement.setBeginLine(ruleViolation.getBeginLine());
                metricsMeasurement.setBeginColumn(ruleViolation.getBeginColumn());
                metricsMeasurement.setEndLine(ruleViolation.getEndLine());
                metricsMeasurement.setEndColumn(ruleViolation.getEndLine());
                metricsMeasurement.setPackageName(ruleViolation.getPackageName());
                metricsMeasurement.setClassName(ruleViolation.getClassName());
                metricsMeasurement.setMethodName(ruleViolation.getMethodName());
                metricsMeasurement.setVariableName(ruleViolation.getVariableName());

                String[] metrics = ruleViolation.getDescription().split(",");
                for (String metric : metrics) {
                    String[] keyValue = metric.split("=");
                    metricsMeasurement.addMetric(keyValue[0], Double.parseDouble(keyValue[1]));
                }

                metricsReport.add(metricsMeasurement);
            }

            @Override
            public void metricAdded(Metric metric) {
                // nothing here
            }
        });

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
        metricsReport.logInfo("Opcodes with asm: %s", asmFields);

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

        MetricsReport report;

        MetricsLogRenderer(MetricsReport report) {
            super("log-metrics", "Metrics logging renderer");
            this.report = report;
        }

        @Override
        public void start() {
            // Nothing to do
        }

        @Override
        public void startFileAnalysis(final DataSource dataSource) {
            //System.out.println("Processing file: " + dataSource.getNiceFileName(false, ""));            
        }

        @Override
        public void renderFileReport(final Report r) {
            if (r.hasErrors()) {
                for (Iterator<ProcessingError> it = r.errors(); it.hasNext(); ) {
                    ProcessingError error = it.next();
                    report.logError(error.getDetail());
                }
            }
            else {
                report.logInfo("Report with size %d collected successfully.", r.size());
            }
        }

        @Override
        public void end() {
            // Nothing to do
        }

        @Override
        public String defaultFileExtension() {
            return null;
        } // not relevant
    }
}
