/*
 * Copyright 2017 Ribose Inc. <https://www.ribose.com>
 * Copyright 2016 M-Way Solutions GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;
import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import hudson.model.Cause;
import hudson.model.TaskListener;
import hudson.util.StreamTaskListener;
import org.apache.commons.lang.time.FastDateFormat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;


public class SQSTriggerBuilder implements Runnable {

    private static final FastDateFormat df = FastDateFormat.getInstance("yyyyMMdd");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final SQSJob job;
    private final Log log;
    private final TaskListener listener;
    private final Message message;

    public SQSTriggerBuilder(final SQSJob job, final Message message) throws IOException {
        this.job = job;
        this.message = message;

        SQSActivityAction activityAction = this.job.getJenkinsJob().getAction(SQSActivityAction.class);

        String date = df.format(new Date());
        String logPath = String.format("%s/triggers-on-%s.log", activityAction.getActivityDir().getPath(), date);

        this.listener = new StreamTaskListener(new File(logPath), true, Charset.forName("UTF-8"));
        this.log = Log.get(SQSTriggerBuilder.class, this.listener.getLogger(), false);

        this.log.info("Try to trigger the build, message-id: %s", StringUtils.getMessageId(message));
        this.log.debug("Print out message-body: %s", this.message.getBody());
    }

    @Override
    public void run() {
        final boolean hasChanges = this.job.poll(listener).hasChanges();
        this.log.info("Any code changes found in SCM? %s", hasChanges);

        if (hasChanges) {
            this.startJob();
        }
        else {
            log.info("Cancel the build since no change found");
        }
    }

    private void startJob() {
        Cause cause = new Cause.RemoteCause("SQSTrigger", String.format("Start job for SQS Message: %n %s", gson.toJson(message)));

        //Job Build can be triggered by 1+ SQS messages because of quiet-period in Jenkins, @see https://jenkins.io/blog/2010/08/11/quiet-period-feature/
        boolean scheduled = job.scheduleBuild(cause);
        this.log.info("Finally! The build is scheduled? %s", scheduled);
        this.log.getStreamHandler().flush();
    }
}
