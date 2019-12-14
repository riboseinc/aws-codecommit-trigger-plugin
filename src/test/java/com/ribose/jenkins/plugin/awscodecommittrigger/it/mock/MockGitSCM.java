package com.ribose.jenkins.plugin.awscodecommittrigger.it.mock;

import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.*;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.SubmoduleConfig;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.browser.GitRepositoryBrowser;
import hudson.plugins.git.extensions.GitSCMExtension;
import hudson.scm.*;
import hudson.triggers.TriggerDescriptor;
import org.apache.commons.io.FileUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @DataBoundConstructor
    public MockGitSCM(
        List<UserRemoteConfig> userRemoteConfigs,
        List<BranchSpec> branches,
        Boolean doGenerateSubmoduleConfigurations,
        Collection<SubmoduleConfig> submoduleCfg,
        GitRepositoryBrowser browser,
        String gitTool,
        List<GitSCMExtension> extensions
    ) {
        super(userRemoteConfigs, branches, doGenerateSubmoduleConfigurations, submoduleCfg, browser, gitTool, extensions);
        this.url = userRemoteConfigs.get(0).getUrl();
    }

    @Override
    public void checkout(Run<?, ?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {
        System.out.println("Mock GitSCM checkout code");
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void buildEnvironment(Run<?, ?> build, java.util.Map<String, String> env) {
        System.out.println("mock git scm function");
    }

    @Override
    public PollingResult compareRemoteRevisionWith(@Nonnull Job<?, ?> project, @Nullable Launcher launcher, @Nullable FilePath workspace, @Nonnull TaskListener listener, @Nonnull SCMRevisionState baseline) throws IOException, InterruptedException {
        return PollingResult.BUILD_NOW;
    }

    public static MockGitSCM fromSqsMessage(String sqsMessage) {
        String url = StringUtils.findByUniqueJsonKey(sqsMessage, "__gitUrl__");
        String branches = StringUtils.findByUniqueJsonKey(sqsMessage, "__gitBranches__");
        if (org.apache.commons.lang3.StringUtils.isBlank(branches)) {
            return new MockGitSCM(url);
        }
        return fromSqsMessage(url, branches);
    }

    public static MockGitSCM fromUrlAndBranchSpecs(String url, List<BranchSpec> branchSpecs) {
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

    public static MockGitSCM fromSqsMessage(String sqsMessage, String branches) {
        String url = StringUtils.findByUniqueJsonKey(sqsMessage, "__gitUrl__");
        List<BranchSpec> branchSpecs = new ArrayList<>();
        for (String branch : branches.split(",")) {
            branchSpecs.add(new BranchSpec(branch));
        }
        return fromUrlAndBranchSpecs(url, branchSpecs);
    }

}
