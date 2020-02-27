package io.jenkins.plugins.metrics.view;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.Action;
import hudson.model.FreeStyleProject;

import io.jenkins.plugins.metrics.testutil.IntegrationTestUtil;

/**
 * Integration test for the class {@link MetricsJobAction}.
 */
public class MetricsJobActionITest {

    /**
     * Jenkins rule for the integration test.
     */
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    /**
     * The MetricsAction should only be displayed after the first build.
     *
     * @throws IOException
     *         test -> ignored
     * @throws SAXException
     *         test -> ignored
     */
    @Test
    public void shouldDisplayMetricsActionAfterFirstBuild() throws IOException, SAXException {
        WebClient webClient = IntegrationTestUtil.getWebClient(jenkins, false);

        FreeStyleProject project = jenkins.createFreeStyleProject();
        HtmlPage projectPage = webClient.getPage(project);
        final String metricsLinkXPath = "//a[@href=\"" + projectPage.getBaseURL().getPath() + "metrics\"]";

        WebAssert.assertElementNotPresentByXPath(projectPage, metricsLinkXPath);

        project.scheduleBuild2(0, new Action[0]);

        projectPage = webClient.getPage(project);
        WebAssert.assertElementPresentByXPath(projectPage, metricsLinkXPath);
    }
}
