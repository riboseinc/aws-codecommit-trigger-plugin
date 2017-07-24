package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.subscribed_branch;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


@Ignore
@RunWith(Parameterized.class)
public class MultiProjectsFixtureIT extends AbstractJenkinsIT {

    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public List<ProjectFixture> projectFixtures;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> fixtures() {

        List<ProjectFixture> withoutWildcardProjects = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String branch = String.format("%s_%s", i, UUID.randomUUID().toString());
            withoutWildcardProjects.add(
                new ProjectFixture()
                    .setSendBranches("refs/heads/" + branch)
                    .setSubscribedBranches(branch)
                    .setShouldStarted(Boolean.TRUE)
            );
        }

        return Arrays.asList(new Object[][]{
            {"should_trigger_branches_without_wildcard", withoutWildcardProjects}
        });
    }

    @Test
    public void shouldPassProjectFixtures() throws Exception {
        logger.log(Level.INFO, "[RUN] " + this.name);
        for (final ProjectFixture projectFixture : this.projectFixtures) {
            this.mockAwsSqs.send(projectFixture.getSendBranches());
        }

        ExecutorService threadPool = Executors.newCachedThreadPool();// newFixedThreadPool(Math.min(this.projectFixtures.size(), 4));

        for (final ProjectFixture fixture : this.projectFixtures) {
            logger.log(Level.FINEST, "[FIXTURE] {0}", fixture);

            threadPool.submit(new Runnable() {

                @Override
                public void run() {
//                    try {
//                        MultiProjectsFixtureIT.this.logger.log(Level.INFO, "[THREAD-STARTED] subscribed branches: {0}", fixture.getSubscribedBranches());
//                        OneShotEvent buildEvent = MultiProjectsFixtureIT.this.submitGitScmProject(MultiProjectsFixtureIT.this.getScm(), fixture.getSubscribedBranches());
//                        buildEvent.block(fixture.getTimeout() * MultiProjectsFixtureIT.this.projectFixtures.size());
//                        fixture.setEvent(buildEvent);
//                        MultiProjectsFixtureIT.this.logger.log(Level.INFO, "[THREAD-DONE] subscribed branches: {0}", fixture.getSubscribedBranches());
//                    } catch (IOException | InterruptedException e) {
//                        throw new AssertionError(e);
//                    }
                }
            });
        }

        threadPool.shutdown();
        threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);

        for (ProjectFixture fixture : this.projectFixtures) {
            logger.log(Level.INFO, "asserting fixture: {0}", fixture);
            Assertions.assertThat(fixture.getEvent()).isNotNull();
            Assertions.assertThat(fixture.getEvent().isSignaled()).isEqualTo(fixture.getShouldStarted());
        }

        logger.log(Level.INFO, "[DONE] " + this.name);
    }
}
