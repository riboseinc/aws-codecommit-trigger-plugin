package com.ribose.jenkins.plugin.awscodecommittrigger.it.mock;

import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.scm.SCMRevisionState;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


public class MockGitSCM extends GitSCM {

    private String url;

    public MockGitSCM(String repositoryUrl) {
        super(repositoryUrl);
        this.url = repositoryUrl;
    }

    public MockGitSCM(List<UserRemoteConfig> userRemoteConfigs, List<BranchSpec> branches, Boolean doGenerateSubmoduleConfigurations, Collection<SubmoduleConfig> submoduleCfg, GitRepositoryBrowser browser, String gitTool, List<GitSCMExtension> extensions) {
        super(userRemoteConfigs, branches, doGenerateSubmoduleConfigurations, submoduleCfg, browser, gitTool, extensions);
        this.url = userRemoteConfigs.get(0).getUrl();
    }

    @Override
    public void checkout(Run<?, ?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {
    }

    public String getUrl() {
        return url;
    }

    public static MockGitSCM fromSqsMessage(String sqsMessage) {
        String url = StringUtils.findByUniqueJsonKey(sqsMessage, "__gitUrl__");
        String branches = StringUtils.findByUniqueJsonKey(sqsMessage, "__gitBranches__");
        if (org.apache.commons.lang3.StringUtils.isBlank(branches)) {
            return new MockGitSCM(url);
        }
        return fromSqsMessage(url, branches);
    }

    public static MockGitSCM fromSqsMessage(String sqsMessage, String branches) {
        String url = StringUtils.findByUniqueJsonKey(sqsMessage, "__gitUrl__");
        List<BranchSpec> branchSpecs = new ArrayList<>();
        for (String branch : branches.split(",")) {
            branchSpecs.add(new BranchSpec(branch));
        }
        return new MockGitSCM(
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
