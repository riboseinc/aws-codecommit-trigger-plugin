package com.ribose.jenkins.plugin.awscodecommittrigger.it;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import hudson.model.Result;
import hudson.model.queue.QueueTaskFuture;
import hudson.util.OneShotEvent;
import org.assertj.core.api.Assertions;
import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

import java.util.UUID;

public abstract class AbstractPipelineTestProject extends AbstractJenkinsTestProject {

    protected static OneShotEvent buildEvent;

    protected void subscribeProject(ProjectFixture fixture) throws Exception {
        String name = UUID.randomUUID().toString();
        WorkflowJob job = jenkinsRule.getInstance().createProject(WorkflowJob.class, name);

        String script = fixture.getPipelineScript().replace("${EmitEvent}", AbstractPipelineTestProject.class.getName() + ".emitBuildEvent()");
        CpsFlowDefinition flowDefinition = new CpsFlowDefinition(script, true);
        job.setDefinition(flowDefinition);

        QueueTaskFuture<WorkflowRun> run = job.scheduleBuild2(0);
        WorkflowRun wfRun = run.get();
        Assertions.assertThat(wfRun.getResult())
            .describedAs("Pipeline unable to start succeed")
            .isEqualTo(Result.SUCCESS);
        jenkinsRule.assertBuildStatusSuccess(wfRun);

        resetPipelineBuildEvent(fixture);

        if (!fixture.isHasTrigger()) {
            return;
        }

        final String uuid = this.sqsQueue.getUuid();
        SQSTrigger trigger = new SQSTrigger(uuid, fixture.isSubscribeInternalScm(), fixture.getScmConfigs());
        job.addTrigger(trigger);
        trigger.start(job, false);
    }

    @Whitelisted
    public static void emitBuildEvent() {
        if (buildEvent != null) {
            buildEvent.signal();
        }
    }

    public synchronized static void resetPipelineBuildEvent(ProjectFixture fixture) {
        buildEvent = new OneShotEvent();
        fixture.setEvent(buildEvent);
    }
}
