package com.ribose.jenkins.plugin.awscodecommittrigger.it.issue._30;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.util.OneShotEvent;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.util.logging.Level;

@Ignore
@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/30")
public class JenkinsIT extends AbstractJenkinsIT {

    private ProjectFixture fixture = new ProjectFixture()
        .withSqsMessage(JenkinsIT.class, "us-east-1.json")
        .setSubscribedBranches("refs/heads/master")
        .setShouldStarted(Boolean.TRUE);

    public SCM getScm() {
        return new GitSCM(this.fixture.getScmUrl());
    }

    @Test
    public void shouldPass() throws IOException, InterruptedException {
        logger.log(Level.INFO, "[RUN] integration test for issue #30");

        this.mockAwsSqs.sendMessage(this.fixture.getSqsMessage());
        OneShotEvent event = this.submitGitScmProject(this.getScm(), fixture.getSubscribedBranches());
        event.block(this.fixture.getTimeout());
        Assertions.assertThat(event.isSignaled()).isEqualTo(this.fixture.getShouldStarted());

        logger.log(Level.INFO, "[DONE]");
    }
}
