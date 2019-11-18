package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.scmconfig;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.AbstractFreestyleParamsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ScmConfigFactory;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.plugins.git.BranchSpec;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JenkinsIT extends AbstractFreestyleParamsIT {

    private final static ScmConfigFactory scmConfigFactory = ScmConfigFactory.get();

    /* Freestyle Job SCM integration test

     -----------------------------------------------------------------------------------------------------------------------
     | Job SCM                          | Trigger Configuration       | Event coming                     | Job should RUN? |
     -----------------------------------------------------------------------------------------------------------------------
     | branch: "refs/head/bar"          | type: "IR"                  | branch: "refs/heads/bar"         | True            | internal scm matched
     -----------------------------------------------------------------------------------------------------------------------
     | branch: "refs/head/bar"          | type: "IR"                  | branch: "refs/heads/foo"         | False           | internal scm not matched
     -----------------------------------------------------------------------------------------------------------------------
     | branch: "refs/head/bar"          | type: "ER"                  | branch: "refs/heads/bar"         | False           | external scm not matched
     |                                  | empty url/branch            |                                  |                 |
     -----------------------------------------------------------------------------------------------------------------------
     | branch: "refs/head/bar"          | type: "ER"                  | branch: "refs/heads/bar"         | True            | external scm matched
     |                                  | branch: "refs/heads/bar"    |                                  |                 |
     -----------------------------------------------------------------------------------------------------------------------

     * */

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> fixtures() {
        return Arrays.asList(new Object[][]{
            {
                "internal scm matched",
                new ProjectFixture()
                    .setSendBranches("refs/heads/bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/bar"))))
                    .setSubscribeInternalScm(true)
                    .setShouldStarted(true)
            },
            {
                "internal scm not matched",
                new ProjectFixture()
                    .setSendBranches("refs/heads/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/bar"))))
                    .setSubscribeInternalScm(true)
                    .setShouldStarted(false)
            },
            {
                "external scm not matched",
                new ProjectFixture()
                    .setSendBranches("refs/heads/bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/bar"))))
                    .setScmConfigs(scmConfigFactory.createERs("", ""))
                    .setSubscribeInternalScm(false)
                    .setShouldStarted(false)
            },
            {
                "external scm matched",
                new ProjectFixture()
                    .setSendBranches("refs/heads/bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/bar"))))
                    .setScmConfigs(scmConfigFactory.createERs(defaultSCMUrl, "refs/heads/bar"))
                    .setSubscribeInternalScm(false)
                    .setShouldStarted(true)
            },
        });
    }
}
