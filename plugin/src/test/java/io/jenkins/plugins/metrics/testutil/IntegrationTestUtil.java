package io.jenkins.plugins.metrics.testutil;

import java.util.logging.Level;

import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.JenkinsRule.WebClient;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;

/**
 * Utility class for integration tests.
 */
public class IntegrationTestUtil {

    /**
     * Get a {@link WebClient} for interacting with the jenkins UI.
     *
     * @param jenkinsRule
     *         the {@link JenkinsRule} for this test
     * @param javaScriptEnabled
     *         true to enable JavaScript, false otherwise
     *
     * @return the {@link WebClient}
     */
    public static WebClient getWebClient(final JenkinsRule jenkinsRule, final boolean javaScriptEnabled) {
        WebClient webClient = jenkinsRule.createWebClient();
        webClient.setCssErrorHandler(new SilentCssErrorHandler());
        java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.SEVERE);
        webClient.setIncorrectnessListener((s, o) -> {
        });

        webClient.setJavaScriptEnabled(javaScriptEnabled);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.getCookieManager().setCookiesEnabled(javaScriptEnabled);
        webClient.getOptions().setCssEnabled(javaScriptEnabled);

        webClient.getOptions().setDownloadImages(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);

        return webClient;
    }
}
