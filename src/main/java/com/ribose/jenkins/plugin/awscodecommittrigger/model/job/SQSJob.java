package com.ribose.jenkins.plugin.awscodecommittrigger.model.job;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import hudson.model.Cause;
import hudson.model.Job;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import hudson.scm.SCM;

import java.util.List;

public interface SQSJob {

    List<SCM> getScmList();

    SQSTrigger getTrigger();

    <J extends Job> J getJenkinsJob();

    boolean scheduleBuild(Cause cause);

    PollingResult poll(TaskListener listener);
}
