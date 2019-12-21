package com.ribose.jenkins.plugin.awscodecommittrigger.interfaces;

import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;

import java.util.List;

public interface ScmFactory {
    GitSCM createGit(String url, List<BranchSpec> branchSpecs);
}
