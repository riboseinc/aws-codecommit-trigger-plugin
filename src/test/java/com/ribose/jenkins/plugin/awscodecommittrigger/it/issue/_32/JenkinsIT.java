package com.ribose.jenkins.plugin.awscodecommittrigger.it.issue._32;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSScmConfig;
import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/32")
public class JenkinsIT extends AbstractJenkinsIT {

    private final MockGitSCM scm;
    private final ProjectFixture fixture;

    public JenkinsIT() throws IOException {
        String sqsMessage = IOUtils.toString(Utils.getResource(this.getClass(), "us-east-1.json"), StandardCharsets.UTF_8);
        this.scm = MockGitSCM.fromSqsMessage(sqsMessage, "refs/heads/master");

        List<SQSScmConfig> scmConfigs = scmConfigFactory.createIR();
        this.fixture = new ProjectFixture()
            .setSqsMessage(sqsMessage)
            .setScmConfigs(scmConfigs)
            .setShouldStarted(Boolean.TRUE);
    }

    @Test
    public void shouldPassIR() throws IOException, InterruptedException {
        this.mockAwsSqs.sendMessage(this.fixture.getSqsMessage());
        this.submitAndAssertFixture(this.scm, fixture);
    }
}
