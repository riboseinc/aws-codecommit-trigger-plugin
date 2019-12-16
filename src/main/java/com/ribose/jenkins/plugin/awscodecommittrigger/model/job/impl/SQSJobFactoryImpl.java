package com.ribose.jenkins.plugin.awscodecommittrigger.model.job.impl;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJobFactory;
import hudson.model.AbstractProject;
import hudson.model.Job;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;

public class SQSJobFactoryImpl implements SQSJobFactory {

    @Override
    public SQSJob createSqsJob(Job job, SQSTrigger sqsTrigger) {
        SQSJob sqsJob = null;
        if (job instanceof AbstractProject) {
            sqsJob = new SQSAbstractProjectJob((AbstractProject) job, sqsTrigger);
        }
        else if (job instanceof WorkflowJob) {
            sqsJob = new SQSWorkflowJob((WorkflowJob) job, sqsTrigger);
        }
        return sqsJob;
    }
}
