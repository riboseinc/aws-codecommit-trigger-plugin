package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.pipeline;

import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractPipelineTestProject;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;


@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/29")
public class PiplineSimpleIT extends AbstractPipelineTestProject {

    private ProjectFixture fixture = new ProjectFixture();

    public PiplineSimpleIT() throws IOException {
        this.fixture
            .setName("issues#29")
            .setPipelineScript(IOUtils.toString(Utils.getResource(PiplineSimpleIT.class, "Jenkinsfile")))
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
