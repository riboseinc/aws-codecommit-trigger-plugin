package com.ribose.jenkins.plugin.awscodecommittrigger.it;

import com.ribose.jenkins.plugin.awscodecommittrigger.InternalInjector;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTriggerQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockAwsSqs;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockContext;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.scm.NullSCM;
import hudson.util.OneShotEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public abstract class AbstractJenkinsIT {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    protected static Logger logger = Logger.getLogger(JenkinsRule.class.getName());

    protected MockAwsSqs mockAwsSqs;
    protected SQSTriggerQueue sqsQueue;

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

        this.sqsQueue = SQSTrigger.DescriptorImpl.get().getSqsQueues().get(0);
    }

    @After
    public void after() {
        this.mockAwsSqs.clearAndShutdown();
    }

    protected void subscribeFreestyleProject(ProjectFixture fixture) throws IOException {
        String name = UUID.randomUUID().toString();

        final FreeStyleProject project = jenkinsRule.getInstance().createProject(FreeStyleProject.class, name);
        project.setScm(new NullSCM());
        if (fixture.getScm() != null) {
            project.setScm(fixture.getScm());
        }

        final String uuid = this.sqsQueue.getUuid();
        final SQSTrigger trigger = new SQSTrigger(uuid, fixture.isSubscribeInternalScm(), fixture.getScmConfigs());

        final OneShotEvent event = new OneShotEvent();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                event.signal();
                trigger.stop();
                return true;
            }
        });
        project.setQuietPeriod(0);

        trigger.start(project, false);
        project.addTrigger(trigger);

        fixture.setEvent(event);
    }

    protected void submitAndAssertFixture(ProjectFixture fixture) throws InterruptedException, IOException {
        this.subscribeFreestyleProject(fixture);
        OneShotEvent event = fixture.getEvent();
        event.block(fixture.getTimeout());
        Assertions.assertThat(event.isSignaled()).isEqualTo(fixture.getShouldStarted());
    }

//    protected void subscribePipelineProject(String pipelineDefinition, ProjectFixture fixture) throws IOException {
////        String name = UUID.randomUUID().toString();
////        fixture.setJenkinsProjectName(name);
//
////        WorkflowJob project = jenkinsRule.getInstance().createProject(WorkflowJob.class, name);
////        project.setDefinition(new Cps);
//    }
}
