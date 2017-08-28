package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature;

import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.scm.SCM;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


@Ignore
@RunWith(Parameterized.class)
public class MultiProjectFixtureIT extends AbstractJenkinsIT {

    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public List<String> sqsMessages;

    @Parameterized.Parameter(2)
    public List<ProjectFixture> fixtures;

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> fixtures() throws IOException {
        String usEast1Json = IOUtils.toString(Utils.getResource(MultiProjectFixtureIT.class, "us-east-1.json", true), StandardCharsets.UTF_8);
        String usEast2Json = IOUtils.toString(Utils.getResource(MultiProjectFixtureIT.class, "us-east-2.json", true), StandardCharsets.UTF_8);

        return Arrays.asList(new Object[][]{
            {
                "test_mixed_scm_jobs",
                Collections.singletonList(usEast1Json),
                Utils.asList(
//                    new ProjectFixture()
//                        .setSqsMessage(usEast1Json)
//                        .setScmConfigs(scmConfigFactory.createIR())
//                        .setShouldStarted(Boolean.TRUE),
//                    new ProjectFixture()
//                        .setSqsMessage(usEast2Json)
//                        .setScmConfigs(scmConfigFactory.createIR())
//                        .setShouldStarted(Boolean.FALSE)
//                    new ProjectFixture()
//                        .setSqsMessage(usEast1BarJson)
//                        .setSendBranches("refs/heads/bar")
//                        .setScmConfigs(scmConfigFactory.createERs(usEast1Scm.getUrl(), "refs/heads/bar"))
//                        .setShouldStarted(Boolean.TRUE)
                )
            },
            {
                "test_internal_scm_jobs",
                Collections.singletonList(usEast1Json),
                Arrays.asList(
//                    new ProjectFixture()
//                        .setSqsMessage(usEast1Json)
//                        .setScmConfigs(scmConfigFactory.createIR())
//                        .setShouldStarted(Boolean.TRUE),
//                    new ProjectFixture()
//                        .setSqsMessage(usEast1Json)
//                        .setScmConfigs(scmConfigFactory.createIR())
//                        .setShouldStarted(Boolean.TRUE),
//                    new ProjectFixture()
//                        .setSqsMessage(usEast2Json)
//                        .setScmConfigs(scmConfigFactory.createIR())
//                        .setShouldStarted(Boolean.FALSE)
                )
            }
        });
    }

    public SCM getScm(ProjectFixture fixture) {
//        if (fixture.getScm() != null) {
//            return fixture.getScm();
//        }
//
//        if (fixture.getSqsMessage() != null) {
//            return MockGitSCM.fromSqsMessage(fixture.getSqsMessage());
//        }
//
//        return defaultSCM;
        return null;
    }

    @Test
    public void shouldPassProjectFixtures() throws Exception {
//        for (String sqsMessage : this.sqsMessages) {
//            if (org.apache.commons.lang3.StringUtils.isNotBlank(sqsMessage)) {
//                this.mockAwsSqs.sendMessage(sqsMessage);
//            }
//        }
//
//        ExecutorService threadPool = Executors.newCachedThreadPool();
//
//        for (final ProjectFixture fixture : this.fixtures) {
//            threadPool.submit(new Runnable() {
//
//                @Override
//                public void run() {
//                    try {
//                        long threadId = Thread.currentThread().getId();
//                        MultiProjectFixtureIT.this.subscribeFreestyleProject(MultiProjectFixtureIT.this.getScm(fixture), fixture);
//                        MultiProjectFixtureIT.this.logger.log(Level.INFO, "[THREAD-{0}] index: {1}", new Object[]{threadId, fixture.getIndex()});
//                        fixture.getEvent().block(fixture.getTimeout());
//                        MultiProjectFixtureIT.this.logger.log(Level.INFO, "[THREAD-{0}] DONE", threadId);
//                    } catch (IOException | InterruptedException e) {
//                        throw new AssertionError(e);
//                    }
//                }
//            });
//        }
//
//        threadPool.shutdown();
//        threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
//
//        for (ProjectFixture fixture : this.fixtures) {
//            this.logger.log(Level.INFO, "[THREAD-{0}] DONE", fixture.getIndex());
//            Assertions.assertThat(fixture.getEvent()).isNotNull();
//            Assertions.assertThat(fixture.getEvent().isSignaled()).isEqualTo(fixture.getShouldStarted());
//        }
    }
}
