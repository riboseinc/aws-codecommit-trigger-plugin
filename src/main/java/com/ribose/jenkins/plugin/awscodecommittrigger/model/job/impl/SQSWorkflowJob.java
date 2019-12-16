package com.ribose.jenkins.plugin.awscodecommittrigger.model.job.impl;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import hudson.model.Cause;
import hudson.model.TaskListener;
import hudson.scm.PollingResult;
import hudson.scm.SCM;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

import java.util.Arrays;
import java.util.List;

public class SQSWorkflowJob extends AbstractSQSJob {

    private final WorkflowJob job;

    public SQSWorkflowJob(WorkflowJob job, SQSTrigger trigger) {
        super(trigger);
        this.job = job;
    }

    @Override
    public List<SCM> getScmList() {
        return Arrays.asList(this.job.getSCMs().toArray(new SCM[]{}));
    }

    @Override
    public WorkflowJob getJenkinsJob() {
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
