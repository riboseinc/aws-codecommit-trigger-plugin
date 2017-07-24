package com.ribose.jenkins.plugin.awscodecommittrigger.it.issue._30;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.util.OneShotEvent;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/30")
public class JenkinsIT extends AbstractJenkinsIT {

    private final ProjectFixture fixture;

    public JenkinsIT() throws IOException {
        String sqsMessage = String.format(
            IOUtils.toString(StringUtils.getResource(JenkinsIT.class, "us-east-1.json.tpl"), StandardCharsets.UTF_8),
            IOUtils.toString(StringUtils.getResource(JenkinsIT.class, "message.json"), StandardCharsets.UTF_8)
        );

        this.fixture = new ProjectFixture()
            .setSqsMessage(sqsMessage)
            .setSubscribedBranches("refs/heads/master")
            .setShouldStarted(Boolean.TRUE);
    }

    public SCM getScm() {
        return new GitSCM(this.fixture.getScmUrl());
    }

    @Test
    public void shouldPassIt() throws IOException, InterruptedException {
        logger.log(Level.INFO, "[RUN] integration test for issue #30");

        this.mockAwsSqs.sendMessage(this.fixture.getSqsMessage());
        OneShotEvent event = this.submitGitScmProject(this.getScm(), fixture.getSubscribedBranches());
        event.block(this.fixture.getTimeout());
        Assertions.assertThat(event.isSignaled()).isEqualTo(this.fixture.getShouldStarted());

        logger.log(Level.INFO, "[DONE]");
    }
}
