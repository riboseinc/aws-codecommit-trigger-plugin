package com.ribose.jenkins.plugin.awscodecommittrigger.model.job.impl;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import hudson.scm.SCM;

import java.util.Arrays;
import java.util.List;

public class SQSAbstractProjectJob extends AbstractSQSJob {

    private final AbstractProject job;

    public SQSAbstractProjectJob(AbstractProject job, SQSTrigger trigger) {
        super(trigger);
        this.job = job;
    }

    @Override
    public List<SCM> getScmList() {
        return Arrays.asList(this.job.getScm());
    }

    @Override
    public AbstractProject getJenkinsJob() {
        return this.job;
    }

    @Override
    public boolean scheduleBuild(Cause cause) {
        return job.scheduleBuild(cause);
    }

    @Override
    public PollingResult poll(TaskListener listener) {
        return this.job.poll(listener);
    }
}
