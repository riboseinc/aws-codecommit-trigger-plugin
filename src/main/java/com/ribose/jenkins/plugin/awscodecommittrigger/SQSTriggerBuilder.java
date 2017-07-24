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
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.util.StreamTaskListener;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;


public class SQSTriggerBuilder implements Runnable {

    private final AbstractProject job;
    private final DateFormat formatter = DateFormat.getDateTimeInstance();

    private final Log log;
    private final StreamTaskListener listener;
    private final Message message;

    public SQSTriggerBuilder(final AbstractProject job, final Message message) throws IOException {
        this.job = job;
        this.message = message;

        File sqsLogFile = this.job.getAction(SQSTriggerActivityAction.class).getSqsLogFile();
        this.listener = new StreamTaskListener(sqsLogFile, true, Charset.forName("UTF-8"));
        this.log = Log.get(SQSTriggerBuilder.class, this.listener.getLogger());

        String body = this.message.getBody();
        String messageId = StringUtils.findByUniqueJsonKey(body, "MessageId");
        this.log.info("Try to trigger the build, messageId: %s", this.job, messageId);
        this.log.debug("Print out message-body: %s", this.job, body);
    }

    @Override
    public void run() {
        final boolean hasChanges = this.job.poll(listener).hasChanges();
        this.log.info("Any code changes found in SCM? %s", this.job, hasChanges);

        if (hasChanges) {
            this.startJob();
        }
        else {
            log.info("Cancel the build since no change found", this.job);
        }
    }

    private void startJob() {
        Cause cause = new Cause.RemoteCause("SQSTrigger", String.format("Start job for SQS Message: %s", message));

        //Job Build can be triggered by 1+ SQS messages because of quiet-period in Jenkins, @see https://jenkins.io/blog/2010/08/11/quiet-period-feature/
        boolean scheduled = job.scheduleBuild(cause);
        this.log.info("Finally! The build is scheduled? %s", this.job, scheduled);
        this.log.getStreamHandler().flush();
    }
}
