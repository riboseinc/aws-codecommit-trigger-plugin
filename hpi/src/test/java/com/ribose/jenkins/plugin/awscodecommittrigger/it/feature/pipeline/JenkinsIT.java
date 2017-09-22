package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.pipeline;

import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractPipelineIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.recipes.WithPlugin;

import java.io.IOException;


@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/29")
public class JenkinsIT extends AbstractPipelineIT {

    private ProjectFixture fixture = new ProjectFixture();

    public JenkinsIT() throws IOException {
        this.fixture
            .setPipelineScript(IOUtils.toString(Utils.getResource(JenkinsIT.class, "Jenkinsfile")))
            .setSubscribeInternalScm(true)
            .setSendBranches("refs/heads/master")
            .setShouldStarted(true);
    }

    @Test
    public void shouldPassIt() throws Exception {
        this.mockAwsSqs.send(this.fixture.getSendBranches());
        this.submitAndAssertFixture(fixture);
    }
}
