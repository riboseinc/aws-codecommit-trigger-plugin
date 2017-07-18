package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.subscribed_branch;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import hudson.util.OneShotEvent;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


@RunWith(Parameterized.class)
public class MultiProjectFixturesIT extends AbstractJenkinsIT {

    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public List<ProjectFixture> projectFixtures;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> fixtures() {
        return Arrays.asList(new Object[][]{
            {
                "should_not_missing_any_projects",
                Arrays.asList(
                    new ProjectFixture()
                        .setSendBranches("refs/heads/foo")
                        .setSubscribedBranches("foo")
                        .setShouldStarted(Boolean.FALSE),
                    new ProjectFixture()
                        .setSendBranches("refs/heads/bar")
                        .setSubscribedBranches("bar")
                        .setShouldStarted(Boolean.TRUE)
                )
            }
        });
    }

    @Test
    public void shouldPassProjectFixtures() throws Exception {
        logger.log(Level.INFO, "[RUN] " + this.name);
        for (final ProjectFixture projectFixture : this.projectFixtures) {
            this.mockAwsSqs.send(projectFixture.getSendBranches());
        }

        ExecutorService threadPool = Executors.newFixedThreadPool(this.projectFixtures.size());

        for (final ProjectFixture projectFixture : this.projectFixtures) {
            logger.log(Level.FINEST, "[FIXTURE] {0}", projectFixture);
            threadPool.submit(new Runnable() {

                @Override
                public void run() {
                    try {
                        MultiProjectFixturesIT.this.logger.log(Level.INFO, "[THREAD-STARTED] subscribed branches: {0}", projectFixture.getSubscribedBranches());
                        OneShotEvent buildEvent = MultiProjectFixturesIT.this.submitGitScmProject(projectFixture.getSubscribedBranches());
                        buildEvent.block(projectFixture.getTimeout());
                        Assertions.assertThat(buildEvent.isSignaled()).isEqualTo(projectFixture.getShouldStarted());
                        MultiProjectFixturesIT.this.logger.log(Level.INFO, "[THREAD-DONE] subscribed branches: {0}", projectFixture.getSubscribedBranches());
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);

        logger.log(Level.INFO, "[DONE] " + this.name);
    }
}
