package com.ribose.jenkins.plugin.awscodecommittrigger.model.job;

import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.util.ArrayList;
import java.util.List;

public class RepoInfo {

    private List<String> codeCommitUrls;
    private List<String> branches;
    private List<String> nonCodeCommitUrls;

    private RepoInfo(){}

    public static RepoInfo fromSqsJob(SQSJob sqsJob) {
        RepoInfo repoInfo = new RepoInfo();

        List<SCM> scms = sqsJob.getScmList();
        List<String> codeCommitUrls = new ArrayList<>();
        List<String> nonCodeCommitUrls = new ArrayList<>();
        List<String> branches = new ArrayList<>();

        for (SCM scm : scms) {
            if (scm instanceof GitSCM) {//TODO refactor to visitor
                GitSCM git = (GitSCM) scm;
                List<RemoteConfig> repos = git.getRepositories();
                for (RemoteConfig repo : repos) {
                    for (URIish urIish : repo.getURIs()) {
                        String url = urIish.toString();
                        if (StringUtils.isCodeCommitRepo(url)) {
                            codeCommitUrls.add(url);
                        }
                        else {
                            nonCodeCommitUrls.add(url);
                        }
                    }
                }

                for (BranchSpec branchSpec : git.getBranches()) {
                    branches.add(branchSpec.getName());
                }
            }
        }

        repoInfo.nonCodeCommitUrls = nonCodeCommitUrls;
        repoInfo.codeCommitUrls = codeCommitUrls;
        repoInfo.branches = branches;
        return repoInfo;
    }

    public List<String> getCodeCommitUrls() {
        return codeCommitUrls;
    }

    public List<String> getBranches() {
        return branches;
    }

    public List<String> getNonCodeCommitUrls() {
        return nonCodeCommitUrls;
    }

    public boolean isHasCodeCommit() {
        return CollectionUtils.isNotEmpty(this.codeCommitUrls);
    }

    public boolean isHasNonCodeCommit() {
        return CollectionUtils.isNotEmpty(this.nonCodeCommitUrls);
    }

    public boolean isNoUrlFound() {
        return CollectionUtils.isEmpty(this.nonCodeCommitUrls) && CollectionUtils.isEmpty(this.codeCommitUrls);
    }
}
