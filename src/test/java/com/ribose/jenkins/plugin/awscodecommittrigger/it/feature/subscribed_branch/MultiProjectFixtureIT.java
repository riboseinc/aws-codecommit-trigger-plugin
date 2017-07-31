package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.subscribed_branch;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.scm.SCM;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


@RunWith(Parameterized.class)
public class MultiProjectFixtureIT extends AbstractJenkinsIT {

    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public List<ProjectFixture> fixtures;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> fixtures() {

        return Arrays.asList(new Object[][]{
            {
                "should_trigger_branches_without_wildcard",
                Arrays.asList(
                    new ProjectFixture()
                        .setSendBranches("refs/heads/foo")
                        .setSubscribedBranches("foobar")
                        .setShouldStarted(Boolean.FALSE),
                    new ProjectFixture()
                        .setSendBranches("refs/heads/foo")
                        .setSubscribedBranches("foo")
                        .setShouldStarted(Boolean.TRUE),
                    new ProjectFixture()
                        .setSendBranches("refs/heads/bar")
                        .setSubscribedBranches("bar")
                        .setShouldStarted(Boolean.TRUE),
                    new ProjectFixture()
                        .setSendBranches("refs/heads/bar/foo")
                        .setSubscribedBranches("bar/foo")
                        .setShouldStarted(Boolean.TRUE),
                    new ProjectFixture()
                        .setSendBranches("refs/heads/bar/foobar/bar")
                        .setSubscribedBranches("bar/foobar/bar")
                        .setShouldStarted(Boolean.TRUE),
                    new ProjectFixture()
                        .setSendBranches("refs/heads/bar/foobar/bar")
                        .setSubscribedBranches("bar/foobar")
                        .setShouldStarted(Boolean.FALSE)
                )
            },
            {
                "should_not_trigger_prefix_wildcard_branches",
                Arrays.asList(
                    new ProjectFixture()
                        .setSendBranches("refs/heads/foo")
                        .setSubscribedBranches("*foo")
                        .setShouldStarted(Boolean.TRUE),
                    new ProjectFixture()
                        .setSendBranches("refs/heads/bar")
                        .setSubscribedBranches("bar")
                        .setShouldStarted(Boolean.TRUE)
                )
            }
        });
    }

    public SCM getScm(ProjectFixture fixture) {
        return fixture.getSqsMessage() != null ? MockGitSCM.fromSqsMessage(fixture.getSqsMessage()) : DefaultSCM;
    }

    @Test
    public void shouldPassProjectFixtures() throws Exception {
        logger.log(Level.INFO, "[RUN] " + this.name);
        for (final ProjectFixture projectFixture : this.fixtures) {
            this.mockAwsSqs.send(projectFixture.getSendBranches());
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();// newFixedThreadPool(Math.min(this.fixtures.size(), 4));

        for (final ProjectFixture fixture : this.fixtures) {
            logger.log(Level.FINE, "[FIXTURE] {0}", fixture);

            threadPool.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        long threadId = Thread.currentThread().getId();
                        MultiProjectFixtureIT.this.subscribeFreestyleProject(MultiProjectFixtureIT.this.getScm(fixture), fixture);
                        MultiProjectFixtureIT.this.logger.log(Level.INFO, "[THREAD-{0}] subscribed branches: {1}", new Object[]{threadId, fixture.getSubscribedBranches()});
                        fixture.getEvent().block(fixture.getTimeout());
                        MultiProjectFixtureIT.this.logger.log(Level.INFO, "[THREAD-{0}] DONE", threadId);
                    } catch (IOException | InterruptedException e) {
                        throw new AssertionError(e);
                    }
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

        for (ProjectFixture fixture : this.fixtures) {
            logger.log(Level.INFO, "asserting fixture: {0}", fixture);
            Assertions.assertThat(fixture.getEvent()).isNotNull();
            Assertions.assertThat(fixture.getEvent().isSignaled()).isEqualTo(fixture.getShouldStarted());
        }

        logger.log(Level.INFO, "[DONE] " + this.name);
    }
}
