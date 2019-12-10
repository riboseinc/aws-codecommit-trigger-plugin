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
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.recipes.LocalData;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class MigrateTo3xJenkinsIT {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

//    @Before
//    public void before() throws IOException {
//        SystemCredentialsProvider provider = SystemCredentialsProvider.getInstance();
//        List<Credentials> globalCredentials = provider.getDomainCredentialsMap().get(Domain.global());
//
//        //StandardAwsCredentials cred = AwsCredentialsHelper.getCredentials(StandardAwsCredentials.class, sqsQueue.getCredentialsId());
//
//        String accountId = "accountId";
//        String secret = "secret";
//        AmazonWebServicesCredentials credential = new AWSCredentialsImpl(
//            CredentialsScope.GLOBAL,
//            UUID.randomUUID().toString(),
//            accountId,
//            secret,
//            "migration testing"
//        );
//
//        globalCredentials.add(credential);
//        provider.save();
//    }

    @Test
    public void shouldNotSeeMigrationButton() throws IOException, SAXException {
        JenkinsRule.WebClient webClient = jenkinsRule.createWebClient();
        DomElement configureSection = webClient.goTo("configure").getElementsByName("AwsCodeCommitTriggerPlugin").get(0);
        List<?> buttons = configureSection.getByXPath("//button[contains(.,'Migration')]");
        Assertions.assertThat(buttons).isEmpty();
    }

    //TODO add test case to display "unsupported migration version"
    @Test
    public void testUnsupportedMigrationVersion() {
        System.out.println("implementing");
    }

    @Test
    @LocalData("v2")
    public void shouldMigrateSQSTriggerQueue() throws IOException, SAXException {
//        String v2File = Utils.getResource(MigrateTo3xJenkinsIT.class, "v2/com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger.xml", true).getFile();
//        FileUtils.copyFileToDirectory(new File(v2File), jenkinsRule.getInstance().getRootDir());
//
//        String credFile = Utils.getResource(MigrateTo3xJenkinsIT.class, "v2/credentials.xml", true).getFile();
//        FileUtils.copyFileToDirectory(new File(credFile), jenkinsRule.getInstance().getRootDir());

        SQSTrigger.DescriptorImpl desc = (SQSTrigger.DescriptorImpl) jenkinsRule.jenkins.getDescriptor(SQSTrigger.class);
        List<SQSTriggerQueue> queues = desc.getSqsQueues();
        for (SQSTriggerQueue queue : queues) {
            String version = queue.getVersion();
            Assertions.assertThat(PluginInfo.checkPluginCompatibility(version)).isFalse();
        }

        JenkinsRule.WebClient webClient = jenkinsRule.createWebClient();
        HtmlPage configurePage = webClient.goTo("configure");
        webClient.setAjaxController(new AjaxController() {
            public boolean processSynchron(HtmlPage page, WebRequest settings, boolean async) {
                return true;
            }
        });

        DomElement configureSection = configurePage.getElementsByName("AwsCodeCommitTriggerPlugin").get(0);
        HtmlButton migrationButton = (HtmlButton) configureSection.getByXPath("//button[contains(.,'Migration')]").get(0);
        migrationButton.click();

        desc = (SQSTrigger.DescriptorImpl) jenkinsRule.jenkins.getDescriptor(SQSTrigger.class);
        queues = desc.getSqsQueues();
        for (SQSTriggerQueue queue : queues) {
            String version = queue.getVersion();
            Assertions.assertThat(PluginInfo.checkPluginCompatibility(version)).isTrue();
//            Assertions.assertThat(StringUtils.checkCompatibility(version, PluginInfo.version)).isTrue();
        }
    }
}
