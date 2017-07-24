package com.ribose.jenkins.plugin.awscodecommittrigger.it.mock;

import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCMRevisionState;

import java.io.File;
import java.io.IOException;

public class MockGitSCM extends GitSCM {
    public MockGitSCM(String repositoryUrl) {
        super(repositoryUrl);
    }

    @Override
    public void checkout(Run<?, ?> build, Launcher launcher, FilePath workspace, TaskListener listener, File changelogFile, SCMRevisionState baseline) throws IOException, InterruptedException {
    }

    public static MockGitSCM fromSqsMessage(String sqsMessage) {
        return new MockGitSCM(StringUtils.findByUniqueJsonKey(sqsMessage, "__gitUrl__"));
    }
}
