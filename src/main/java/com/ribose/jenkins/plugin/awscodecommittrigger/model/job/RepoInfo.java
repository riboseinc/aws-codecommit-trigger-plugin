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

    private List<String> urls;
    private List<String> branches;

    private RepoInfo(){}

    public static RepoInfo fromSqsJob(SQSJob sqsJob) {
        RepoInfo repoInfo = new RepoInfo();

        List<SCM> scms = sqsJob.getScmList();
        List<String> urls = new ArrayList<>();
        List<String> branches = new ArrayList<>();
        for (SCM scm : scms) {
            if (scm instanceof GitSCM) {//TODO refactor to visitor
                GitSCM git = (GitSCM) scm;
                List<RemoteConfig> repos = git.getRepositories();
                for (RemoteConfig repo : repos) {
                    for (URIish urIish : repo.getURIs()) {
                        String url = urIish.toString();
                        if (StringUtils.isCodeCommitRepo(url)) {
                            urls.add(url);
                        }
//                        urls.add(url);
//                        if (!repoInfo.hasCodeCommit && StringUtils.isCodeCommitRepo(url)) {
//                            repoInfo.hasCodeCommit = true;
//                        }
                    }
                }

                for (BranchSpec branchSpec : git.getBranches()) {
                    branches.add(branchSpec.getName());
                }
            }
        }

        repoInfo.urls = urls;
        repoInfo.branches = branches;
        return repoInfo;
    }



    public List<String> getUrls() {
        return urls;
    }

    public List<String> getBranches() {
        return branches;
    }

    public boolean isHasCodeCommit() {
        return CollectionUtils.isNotEmpty(this.urls);
    }
}
