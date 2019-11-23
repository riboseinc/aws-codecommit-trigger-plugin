package com.ribose.jenkins.plugin.awscodecommittrigger.it.mock;

import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.ScmFactory;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;

import java.util.List;

public class MockScmFactory implements ScmFactory {

    public MockScmFactory() {
    }

    @Override
    public GitSCM createGit(String url, List<BranchSpec> branchSpecs) {
        return MockGitSCM.fromUrlAndBranchSpecs(url, branchSpecs);
    }
}
