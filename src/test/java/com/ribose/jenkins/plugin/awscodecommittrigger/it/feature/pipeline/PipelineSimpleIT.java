package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.pipeline;

import com.ribose.jenkins.plugin.awscodecommittrigger.Utils;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractPipelineTestProject;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Ignore("IGNORED due to mock-git-scm not work with new pipeline")
@Issue("riboseinc/aws-codecommit-trigger-plugin/issues/29")
public class PipelineSimpleIT extends AbstractPipelineTestProject {

    private ProjectFixture fixture = new ProjectFixture();

    public PipelineSimpleIT() throws IOException {
        this.fixture
            .setName("issues#29")
            .setPipelineScript(IOUtils.toString(Utils.getResource(PipelineSimpleIT.class, "Jenkinsfile"), StandardCharsets.UTF_8))
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
