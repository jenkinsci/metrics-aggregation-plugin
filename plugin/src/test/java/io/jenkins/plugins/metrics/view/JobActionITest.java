package io.jenkins.plugins.metrics.view;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;
import org.xml.sax.SAXException;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebAssert;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import hudson.model.Action;
import hudson.model.FreeStyleProject;

import io.jenkins.plugins.metrics.testutil.IntegrationTestUtil;

import static org.assertj.core.api.Assertions.*;

public class JobActionITest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

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
