package com.ribose.jenkins.plugin.awscodecommittrigger.model.job;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import hudson.model.Job;

public interface SQSJobFactory {
    SQSJob createSqsJob(Job job, SQSTrigger sqsTrigger);
}
