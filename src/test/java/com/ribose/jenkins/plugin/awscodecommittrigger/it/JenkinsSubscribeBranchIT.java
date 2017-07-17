/*
 * Copyright 2017 Ribose Inc. <https://www.ribose.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ribose.jenkins.plugin.awscodecommittrigger.it;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockAwsSqs;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockSQSFactory;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.util.OneShotEvent;
import com.ribose.jenkins.plugin.awscodecommittrigger.Context;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTriggerQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueueMonitorScheduler;
import com.ribose.jenkins.plugin.awscodecommittrigger.threading.SQSQueueMonitorSchedulerImpl;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Starts an embedded servlet container so that the test code can exercise user interaction through HTTP and assert based on the outcome.
 *
 * https://wiki.jenkins-ci.org/display/JENKINS/Unit+Test#UnitTest-Overview
 */
@RunWith(Parameterized.class)
public class JenkinsSubscribeBranchIT {

    private final static Logger LOG = Logger.getLogger(JenkinsSubscribeBranchIT.class.getName());

    @Parameters(name = "{0}")
    public static List<Object[]> fixtures() {
        return Arrays.asList(new Object[][]{
            {
                "should_trigger_branches_without_wildcard",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo")
                    .setListenBranches("foo")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_not_trigger_prefix_wildcard_branches",
                new ProjectFixture()//prefix wildcard
                    .setSendBranches("refs/heads/foo-bar", "refs/heads/bar/foo", "refs/heads/foo/bar")
                    .setListenBranches("*foo")
                    .setShouldStarted(Boolean.FALSE)
            },
            {
                "should_trigger_prefix_wildcard_branches",
                new ProjectFixture()//prefix wildcard
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/bar-foo")
                    .setListenBranches("*foo")
                    .setShouldStarted(Boolean.TRUE),//triggered because of msg "refs/heads/bar-foo"

            },
            {
                "should_not_trigger_suffix_wildcard_branches",
                new ProjectFixture()//suffix wildcard
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar-foo")
                    .setListenBranches("foo*")
                    .setShouldStarted(Boolean.FALSE)
            },
            {
                "should_trigger_suffix_wildcard_branches",
                new ProjectFixture()//suffix wildcard
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/foo-bar")
                    .setListenBranches("foo*")
                    .setShouldStarted(Boolean.TRUE),//triggered because of msg "refs/heads/foo-bar"
            },
            {
                "should_not_trigger_single_star_branches",
                new ProjectFixture()// "*"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar/foo")
                    .setListenBranches("*")
                    .setShouldStarted(Boolean.FALSE),
            },
            {
                "should_trigger_single_star_branches",
                new ProjectFixture()// "*"
                    .setSendBranches("refs/heads/foo", "refs/heads/foo-bar")
                    .setListenBranches("*")
                    .setShouldStarted(Boolean.TRUE),
            },
            {
                "should_not_trigger_double_stars_branches",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/bar/foo", "refs/heads/bar/foo-bar", "refs/heads/bar/foo/bar")
                    .setListenBranches("foo**")
                    .setShouldStarted(Boolean.FALSE),
            },
            {
                "should_trigger_double_stars_branches",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/foo-bar")
                    .setListenBranches("foo**")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_trigger_all_branches",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar/foo", "refs/heads/foo", "refs/heads/foo-bar")
                    .setListenBranches("**")
                    .setShouldStarted(Boolean.TRUE),
            }
        });
    }

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    private MockAwsSqs mockAwsSqs;
    private SQSTriggerQueue sqsQueue;

    private final ProjectFixture projectFixture;
    private final String name;

    private final MockSQSFactory mockSQSFactory = MockSQSFactory.get();
    private final SCM gitScm = new GitSCM(MockResource.get().getGitUrl());

    public JenkinsSubscribeBranchIT(String name, ProjectFixture projectFixture) {
        this.projectFixture = projectFixture;
        this.name = name;
    }

    @Test
    public void shouldPassProjectFixture() throws Exception {
        LOG.log(Level.INFO, "[RUN] " + this.name);
        LOG.log(Level.FINEST, "[FIXTURE] " + this.projectFixture);
        this.mockAwsSqs.send(this.projectFixture.getSendBranches());
        OneShotEvent buildStarted = createFreestyleProject(this.projectFixture.getListenBranches());
        buildStarted.block(this.projectFixture.getTimeout());
        Assertions.assertThat(buildStarted.isSignaled()).isEqualTo(this.projectFixture.getShouldStarted());
        LOG.log(Level.INFO, "[DONE] " + this.name);
    }

//    @Test
//    public void shouldPassMultiProjectFixtures() throws Exception {
////        Assume.assumeNotNull(this.projectFixtures);
////
////        before(Executors.newFixedThreadPool(this.projectFixtures.size()));
////
////        LOG.log(Level.INFO, "[RUN] " + this.name);
////        List<Future> tasks = new ArrayList<>(this.projectFixtures.size());
////        for (ProjectFixture projectFixture : this.projectFixtures) {
////            OneShotEvent event = createFreestyleProject(projectFixture.getListenBranches());
////            ProjectFixture.AssertionsTask task = projectFixture.new AssertionsTask(event);
////            executors.submit(task);
////            tasks.add(task);
////        }
////        for (Future task : tasks) {
////            task.get();
////        }
////        LOG.log(Level.INFO, "[DONE] " + this.name);
//    }

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
        this.mockAwsSqs.shutdown();
    }

    private OneShotEvent createFreestyleProject(String listenBranches) throws IOException {
        final FreeStyleProject project = jenkinsRule.createFreeStyleProject(UUID.randomUUID().toString());
        project.setScm(this.gitScm);

        final String uuid = this.sqsQueue.getUuid();
        final SQSTrigger trigger = new SQSTrigger(uuid, listenBranches);

        final OneShotEvent buildStarted = new OneShotEvent();
        project.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                buildStarted.signal();
                trigger.stop();
                return true;
            }
        });

        trigger.start(project, false);
        project.addTrigger(trigger);
        return buildStarted;
    }
}
