package io.jenkins.plugins.metrics.view;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.Action;
import hudson.model.FreeStyleProject;

import io.jenkins.plugins.metrics.testutil.IntegrationTestUtil;

import static org.assertj.core.api.Assertions.*;

public class MetricsDetailViewITest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shoudShowEmptyMetricsPage() throws IOException, SAXException {
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.scheduleBuild2(0, new Action[0]);

        WebClient webClient = IntegrationTestUtil.getWebClient(jenkins, false);
        HtmlPage metricsPage = webClient.getPage(project, "metrics");

        HtmlDivision projectOverview = getCardByCSSClass(metricsPage, "project-overview");
        HtmlDivision metricsDetails = getCardByCSSClass(metricsPage, "metrics-detail");
        HtmlDivision metricsDistribution = getCardByCSSClass(metricsPage, "metrics-distribution");

        assertThat(projectOverview.getTextContent()).contains("No data available");
        assertThat(metricsDetails.getTextContent()).contains("No data available");
        assertThat(metricsDistribution.getTextContent()).contains("No data available");
    }

    private HtmlDivision getCardByCSSClass(final HtmlPage page, final String cssClass) {
        return page.getFirstByXPath("//*[@id=\"main-panel\"]//div[contains(@class,\"" + cssClass + "\")]");
    }
}
