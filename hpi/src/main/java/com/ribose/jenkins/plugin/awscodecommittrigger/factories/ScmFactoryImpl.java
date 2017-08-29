package com.ribose.jenkins.plugin.awscodecommittrigger.factories;

import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.ScmFactory;
import hudson.Extension;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.extensions.GitSCMExtension;

import java.util.Collections;
import java.util.List;

public class ScmFactoryImpl implements ScmFactory {

    public GitSCM createGit(String url, List<BranchSpec> branchSpecs) {
        return new GitSCM(
            GitSCM.createRepoList(url, null),
            branchSpecs,
            false,
            Collections.<SubmoduleConfig>emptyList(),
            null,
            null,
            Collections.<GitSCMExtension>emptyList()
        );
    }
}
