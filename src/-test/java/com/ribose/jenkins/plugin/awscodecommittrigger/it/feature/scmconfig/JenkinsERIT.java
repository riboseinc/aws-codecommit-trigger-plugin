package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.scmconfig;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.AbstractFreestyleParamsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ScmConfigFactory;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.plugins.git.BranchSpec;
import org.junit.Test;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JenkinsERIT extends AbstractFreestyleParamsIT {

    private final static ScmConfigFactory scmConfigFactory = ScmConfigFactory.get();

    /* Freestyle Job SCM integration test (type="ER")

     ------------------------------------------------------------------------------------------------------------------------------------
     | Job SCM                              | Trigger Configuration             | Event coming                      | Job should RUN?   |
     ------------------------------------------------------------------------------------------------------------------------------------
     | branch: "ref/heads/foo"              | git, branch: {same job scm}       | branch: "ref/heads/foo"           | True              | git url & branch matched
     ------------------------------------------------------------------------------------------------------------------------------------
     | branch: "ref/heads/foo"              | git: {same job scm}               | branch: "ref/heads/foo"           | False             | git url match but branch not matched
     |                                      | branch: ref/heads/bar             |                                   |                   |
     ------------------------------------------------------------------------------------------------------------------------------------
     | branch: "ref/heads/foo"              | git: {not same job scm}           | branch: "ref/heads/foo"           | False             | git url not match but branch matched
     |                                      | branch: ref/heads/foo             |                                   |                   |
     ------------------------------------------------------------------------------------------------------------------------------------
     | {no scm configured}                  | branch: ref/heads/foo             | branch: "ref/heads/foo"           | True              | no job scm configured
     ------------------------------------------------------------------------------------------------------------------------------------

     */

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> fixtures() {
        String gitUrl = defaultSCMUrl + "_diff";
        return Arrays.asList(new Object[][]{
            {
                "git url & branch matched",
                new ProjectFixture()
                    .setSendBranches("refs/heads/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/foo"))))
                    .setScmConfigs(scmConfigFactory.createERs(defaultSCMUrl, "refs/heads/foo"))
                    .setShouldStarted(true)
            },
            {
                "git url match but branch not matched",
                new ProjectFixture()
                    .setSendBranches("refs/heads/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/foo"))))
                    .setScmConfigs(scmConfigFactory.createERs(defaultSCMUrl, "refs/heads/bar"))
                    .setShouldStarted(false)
            },
            {
                "git url not match but branch matched",
                new ProjectFixture()
                    .setSendBranches("refs/heads/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/foo"))))
                    .setScmConfigs(scmConfigFactory.createERs(gitUrl, "refs/heads/foo"))
                    .setShouldStarted(false)
            },
            {
                "no job scm configured",
                new ProjectFixture()
                    .setSendBranches("refs/heads/foo")
                    .setScmConfigs(scmConfigFactory.createERs(defaultSCMUrl, "refs/heads/foo"))
                    .setShouldStarted(true)
            },
        });
    }

    @Test
    public void shouldPassIt() throws Exception {
        this.fixture.setSubscribeInternalScm(false);
        super.shouldPassIt();
    }
}
