package com.ribose.jenkins.plugin.awscodecommittrigger.it;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.ribose.jenkins.plugin.awscodecommittrigger.Context;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTriggerQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueueMonitorScheduler;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockAwsSqs;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockSQSFactory;
import com.ribose.jenkins.plugin.awscodecommittrigger.threading.SQSQueueMonitorSchedulerImpl;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.scm.SCM;
import hudson.util.OneShotEvent;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Logger;

public abstract class AbstractJenkinsIT {

    protected Logger logger = Logger.getLogger(JenkinsRule.class.getName());

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    protected MockAwsSqs mockAwsSqs;
    protected SQSTriggerQueue sqsQueue;

    protected final MockSQSFactory mockSQSFactory = MockSQSFactory.get();

    @Before
    public void before() throws Exception {
        this.mockAwsSqs = MockAwsSqs.get();

        ((SQSQueueMonitorSchedulerImpl) Context.injector().getBinding(SQSQueueMonitorScheduler.class).getProvider().get()).setFactory(this.mockSQSFactory);

        jenkinsRule.getInstance().getDescriptorByType(SQSTriggerQueue.DescriptorImpl.class).setFactory(this.mockSQSFactory);

        //TODO refactor not to use func HtmlForm.submit
        final HtmlForm configForm = jenkinsRule.createWebClient().goTo("configure").getFormByName("config");
        configForm.getInputByName("_.url").setValueAttribute(this.mockAwsSqs.getSqsUrl());

        jenkinsRule.submit(configForm);

        this.sqsQueue = SQSTrigger.DescriptorImpl.get().getSqsQueues().get(0);
        this.sqsQueue.setFactory(this.mockSQSFactory);
    }

    @After
    public void after() {
        this.mockAwsSqs.clearMessages();
        this.mockAwsSqs.shutdown();
    }

    protected OneShotEvent submitJenkinsProject(SCM scm, String subscribedBranches) throws IOException {
        final FreeStyleProject project = jenkinsRule.getInstance().createProject(FreeStyleProject.class, UUID.randomUUID().toString());
        project.setScm(scm);

        final String uuid = this.sqsQueue.getUuid();
        final SQSTrigger trigger = new SQSTrigger(uuid, subscribedBranches);

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
        return event;
    }

    protected void submitAndAssertFixture(SCM scm, ProjectFixture fixture) throws InterruptedException, IOException {
        OneShotEvent event = this.submitJenkinsProject(scm, fixture.getSubscribedBranches());
        event.block(fixture.getTimeout());
        Assertions.assertThat(event.isSignaled()).isEqualTo(fixture.getShouldStarted());
    }
}