package com.ribose.jenkins.plugin.awscodecommittrigger.it.issue._30;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSScmConfig;
import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.plugins.git.GitSCM;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;


@Ignore
@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/30")
public class JenkinsIT extends AbstractJenkinsIT {

//    private final ProjectFixture fixture;
    private final GitSCM scm;

    public JenkinsIT() throws IOException {
        String sqsMessage = IOUtils.toString(Utils.getResource(this.getClass(), "us-east-1.json"), StandardCharsets.UTF_8);
        this.scm = MockGitSCM.fromSqsMessage(sqsMessage);

//        List<SQSScmConfig> scmConfigs = scmConfigFactory.createERs(MockGitSCM.class.cast(this.scm).getUrl(), "refs/heads/master");
//        this.fixture = new ProjectFixture()
//            .setSqsMessage(sqsMessage)
//            .setScmConfigs(scmConfigs)
//            .setShouldStarted(Boolean.TRUE);
    }

    @Test
    public void shouldPassIt() throws IOException, InterruptedException {
//        this.mockAwsSqs.sendMessage(this.fixture.getSqsMessage());
//        this.submitAndAssertFixture(this.scm, fixture);
    }
}
