package com.ribose.jenkins.plugin.awscodecommittrigger.it.issue._30;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSScmConfig;
import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.plugins.git.GitSCM;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;

@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/30")
public class JenkinsIT extends AbstractJenkinsIT {

    private final ProjectFixture fixture;
    private final GitSCM scm;

    public JenkinsIT() throws IOException {
        this.fixture = new ProjectFixture()
            .setSqsMessage(IOUtils.toString(Utils.getResource(JenkinsIT.class, "us-east-1.json"), StandardCharsets.UTF_8))
            //.setSubscribedBranches("refs/heads/master")
            .setScmConfigs(Arrays.asList(new SQSScmConfig(SQSScmConfig.Type.IR, MockGitSCM.class.cast(defaultSCM).getUrl(),"refs/heads/master")))
            .setShouldStarted(Boolean.TRUE);
        this.scm = MockGitSCM.fromSqsMessage(this.fixture.getSqsMessage());
    }

    @Test
    public void shouldPassIt() throws IOException, InterruptedException {
        logger.log(Level.INFO, "[RUN] Integration test for issue #30");
        this.mockAwsSqs.sendMessage(this.fixture.getSqsMessage());
        this.submitAndAssertFixture(this.scm, fixture);
        logger.log(Level.INFO, "[DONE]");
    }
}
