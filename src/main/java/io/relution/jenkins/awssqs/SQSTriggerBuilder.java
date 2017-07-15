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

package io.relution.jenkins.awssqs;

import com.amazonaws.services.sqs.model.Message;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import hudson.util.StreamTaskListener;
import plugins.jenkins.awssqs.exception.UnexpectedException;
import plugins.jenkins.awssqs.utils.StringUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.Date;


public class SQSTriggerBuilder implements Runnable {

    private final AbstractProject job;
    private final DateFormat formatter = DateFormat.getDateTimeInstance();

    private final String messageId;
    private final PrintStream logger;
    private final StreamTaskListener listener;

    public SQSTriggerBuilder(final AbstractProject job, final Message message) throws IOException {
        if (job == null) {
            throw new UnexpectedException("Unexpected error, 'job' object was null!");
        }

        this.job = job;
        this.messageId = StringUtils.findByUniqueJsonKey(message.getBody(), "MessageId");

        this.listener = new StreamTaskListener(
            this.job.getAction(SQSTriggerActivityAction.class).getSqsLogFile(),
            true,
            Charset.forName("UTF-8")
        );
        this.logger = this.listener.getLogger();
    }

    @Override
    public void run() {
        logger.format("%nRunning Job '%s' for Message '%s'%n", job.getName(), messageId);
        this.buildIfChanged();
    }

    //TODO review this condition
    private void buildIfChanged() {
        final long now = System.currentTimeMillis();

        logger.format("Started on %s", this.toDateTime(now));

        final boolean hasChanges = this.job.poll(listener).hasChanges();

        if (hasChanges) {
            logger.println("Changes found");
            this.startJob(now);
        } else {
            logger.println("No changes");
        }

        logger.println("[INFO] Done. Took " + this.toTimeSpan(now));
    }

    private void startJob(final long now) {
        Cause cause = new Cause.RemoteCause("SQS trigger", String.format("Triggering Job for SQS Message [%s] on [%s]", messageId, this.toDateTime(now)));

        //Job Build can be triggered by 1+ SQS messages because of quiet-period in Jenkins, @see https://jenkins.io/blog/2010/08/11/quiet-period-feature/
        if (job.scheduleBuild(cause)) {
            logger.println("Job queued");
        } else {
            logger.println("Job NOT queued - it was determined that this job has been queued already.");
        }

        logger.format("Job '%s' is queued? or is building?: %s , %s%n", job.getName(), job.isInQueue(), job.isBuilding());
        logger.println("Triggering job [COMPLETED]");
    }

    private String toDateTime(final long timestamp) {
        final Date date = new Date(timestamp);
        return this.formatter.format(date);
    }

    private String toTimeSpan(final long timestamp) {
        final long now = System.currentTimeMillis();
        return Util.getTimeSpanString(now - timestamp);
    }
}
