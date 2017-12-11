package com.ribose.jenkins.plugin.awscodecommittrigger.model.job.impl;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;

public abstract class AbstractSQSJob implements SQSJob {

    protected final SQSTrigger trigger;

    public AbstractSQSJob(SQSTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public SQSTrigger getTrigger() {
        return this.trigger;
    }
}
