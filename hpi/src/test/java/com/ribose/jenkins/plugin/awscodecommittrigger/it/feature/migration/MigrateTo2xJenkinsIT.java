package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.migration;

import com.gargoylesoftware.htmlunit.AjaxController;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.ribose.jenkins.plugin.awscodecommittrigger.PluginInfo;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTriggerQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MigrateTo2xJenkinsIT {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Before
    public void before() throws IOException {
        String v1File = Utils.getResource(MigrateTo2xJenkinsIT.class, "com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger.xml", true).getFile();
        FileUtils.copyFileToDirectory(new File(v1File), jenkinsRule.getInstance().getRootDir());
    }

    @Test
    public void shouldMigrateSQSTriggerQueue() throws IOException, SAXException {
        JenkinsRule.WebClient webClient = jenkinsRule.createWebClient();
        HtmlPage configurePage = webClient.goTo("configure");
        webClient.setAjaxController(new AjaxController() {
            public boolean processSynchron(HtmlPage page, WebRequest settings, boolean async) {
                return true;
            }
        });

        DomElement configureSection = configurePage.getElementsByName("AwsCodeCommitTriggerPlugin").get(0);
        SQSTrigger.DescriptorImpl desc = (SQSTrigger.DescriptorImpl) jenkinsRule.jenkins.getDescriptor(SQSTrigger.class);
        List<SQSTriggerQueue> queues = desc.getSqsQueues();
        for (SQSTriggerQueue queue : queues) {
            String version = queue.getVersion();
            Assertions.assertThat(StringUtils.checkCompatibility(version, PluginInfo.version)).isFalse();
        }

        HtmlButton migrationButton = (HtmlButton) configureSection.getByXPath("//button[contains(.,\"Migration\")]").get(0);
        migrationButton.click();

        desc = (SQSTrigger.DescriptorImpl) jenkinsRule.jenkins.getDescriptor(SQSTrigger.class);
        queues = desc.getSqsQueues();
        for (SQSTriggerQueue queue : queues) {
            String version = queue.getVersion();
            Assertions.assertThat(StringUtils.checkCompatibility(version, PluginInfo.version)).isTrue();
        }
    }
}
