package com.ribose.jenkins.plugin.awscodecommittrigger.it.issue._32;

import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractFreestyleTestProject;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.plugins.git.GitSCM;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/32")
public class Issue32IT extends AbstractFreestyleTestProject {

    private final ProjectFixture fixture;

    public Issue32IT() throws IOException {
        String sqsMessage = IOUtils.toString(Utils.getResource(this.getClass(), "us-east-1.json"), StandardCharsets.UTF_8);
        GitSCM scm = MockGitSCM.fromSqsMessage(sqsMessage, "refs/heads/master");
        this.fixture = new ProjectFixture()
            .setSqsMessage(sqsMessage)
            .setSubscribeInternalScm(true)
            .setScm(scm)
            .setShouldStarted(true);
    }

    @Test
    public void shouldPassIt() throws Exception {
        this.mockAwsSqs.sendMessage(this.fixture.getSqsMessage());
        this.submitAndAssertFixture(fixture);
    }
}
