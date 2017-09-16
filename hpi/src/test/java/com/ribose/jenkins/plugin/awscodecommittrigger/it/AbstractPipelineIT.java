package com.ribose.jenkins.plugin.awscodecommittrigger.it;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.OneShotEvent;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.UUID;

public class AbstractPipelineIT extends AbstractJenkinsIT {

    protected static OneShotEvent buildEvent;

    protected void subscribeProject(ProjectFixture fixture) throws Exception {
        String name = UUID.randomUUID().toString();

        WorkflowJob job = jenkinsRule.getInstance().createProject(WorkflowJob.class, name);
        CpsFlowDefinition flowDefinition = new CpsFlowDefinition(
            fixture.getPipelineScript().replace("${EmitEvent}", AbstractPipelineIT.class.getName() + ".emitBuildEvent()"),
            true
        );
        job.setDefinition(flowDefinition);

        QueueTaskFuture<WorkflowRun> run = job.scheduleBuild2(0);
        jenkinsRule.assertBuildStatusSuccess(run);

        resetPipelineBuildEvent(fixture);

        if (!fixture.isHasTrigger()) {
            return;
        }

        final String uuid = this.sqsQueue.getUuid();
        SQSTrigger trigger = new SQSTrigger(uuid, fixture.isSubscribeInternalScm(), fixture.getScmConfigs());
        trigger.start(job, false);
        job.addTrigger(trigger);
    }

    @Whitelisted
    public synchronized static void emitBuildEvent() {
        if (buildEvent != null) {
            buildEvent.signal();
        }
    }

    public static synchronized void resetPipelineBuildEvent(ProjectFixture fixture) {
        buildEvent = new OneShotEvent();
        fixture.setEvent(buildEvent);
    }
}
