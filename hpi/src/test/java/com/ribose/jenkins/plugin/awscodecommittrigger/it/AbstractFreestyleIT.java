package com.ribose.jenkins.plugin.awscodecommittrigger.it;

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleProject;
import hudson.scm.NullSCM;
import hudson.util.OneShotEvent;
import org.jvnet.hudson.test.TestBuilder;

import java.io.IOException;
import java.util.UUID;

public class AbstractFreestyleIT extends AbstractJenkinsIT {

    protected void subscribeProject(ProjectFixture fixture) throws Exception {
        String name = UUID.randomUUID().toString();

        final FreeStyleProject job = jenkinsRule.getInstance().createProject(FreeStyleProject.class, name);
        job.setScm(new NullSCM());
        if (fixture.getScm() != null) {
            job.setScm(fixture.getScm());
        }

        final String uuid = this.sqsQueue.getUuid();

        SQSTrigger trigger = null;

        if (fixture.isHasTrigger()) {
            trigger = new SQSTrigger(uuid, fixture.isSubscribeInternalScm(), fixture.getScmConfigs());
        }

        final OneShotEvent event = new OneShotEvent();
        job.getBuildersList().add(new TestBuilder() {

            @Override
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
                event.signal();
                return true;
            }
        });
        job.setQuietPeriod(0);

        if (trigger != null) {
            trigger.start(job, false);
            job.addTrigger(trigger);
        }

        fixture.setEvent(event);
    }
}
