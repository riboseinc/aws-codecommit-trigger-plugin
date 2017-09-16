package com.ribose.jenkins.plugin.awscodecommittrigger.it;

import com.ribose.jenkins.plugin.awscodecommittrigger.InternalInjector;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTriggerQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockAwsSqs;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockContext;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.plugins.git.GitSCM;
import hudson.util.OneShotEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public abstract class AbstractJenkinsIT {
    protected static Logger logger = Logger.getLogger(JenkinsRule.class.getName());

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    protected MockAwsSqs mockAwsSqs;

    protected SQSTriggerQueue sqsQueue;

    protected static final GitSCM defaultSCM;
    protected static final String defaultSqsMessageTemplate;
    protected static final String defaultSCMUrl;

    static {
        try {
            defaultSqsMessageTemplate = IOUtils.toString(Utils.getResource(AbstractJenkinsIT.class, "sqsmsg.json.tpl", true), StandardCharsets.UTF_8);
            defaultSCM = MockGitSCM.fromSqsMessage(defaultSqsMessageTemplate);

            defaultSCMUrl = ((MockGitSCM) defaultSCM).getUrl();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Before
    public void before() throws Exception {
        this.mockAwsSqs = MockAwsSqs.get();

        jenkinsRule.getInstance().lookup.set(InternalInjector.class, MockContext.getInjector());

        File workDir = jenkinsRule.getInstance().getRootDir();
        String configName = "com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger.xml";
        File configFile = new File(Utils.getResource(AbstractJenkinsIT.class, configName, true).toURI());
        FileUtils.copyFileToDirectory(configFile, workDir);
        configFile = new File(FilenameUtils.concat(workDir.getPath(), configName));

        String config = IOUtils.toString(configFile.toURI(), "UTF-8").replace("${URL}", mockAwsSqs.getSqsUrl());
        IOUtils.write(config, new FileOutputStream(configFile), "UTF-8");

        this.sqsQueue = ((SQSTrigger.DescriptorImpl) jenkinsRule.jenkins.getDescriptor(SQSTrigger.class)).getSqsQueues().get(0);//SQSTrigger.DescriptorImpl.get().getSqsQueues().get(0);

        this.mockAwsSqs.setSqsMessageTemplate(defaultSqsMessageTemplate);
    }

    @After
    public void after() {
        this.mockAwsSqs.clearAndShutdown();
    }

    protected abstract void subscribeProject(ProjectFixture fixture) throws Exception;

    protected void submitAndAssertFixture(ProjectFixture fixture) throws Exception {
        this.subscribeProject(fixture);
        OneShotEvent event = fixture.getEvent();
        event.block(fixture.getTimeout());
        Assertions.assertThat(event.isSignaled()).isEqualTo(fixture.getShouldStarted());
    }
}
