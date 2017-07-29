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
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.ribose.jenkins.plugin.awscodecommittrigger.exception.UnexpectedException;
import com.ribose.jenkins.plugin.awscodecommittrigger.i18n.sqstrigger.Messages;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.*;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.events.ConfigurationChangedEvent;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.events.EventBroker;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJobFactory;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.SequentialExecutionQueue;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SQSTrigger extends Trigger<Job<?, ?>> implements SQSQueueListener {

    private static final Log log = Log.get(SQSTrigger.class);

    private String queueUuid;
    private String subscribedBranches;

    @Inject
    private transient SQSQueueMonitorScheduler scheduler;

    @Inject
    private transient MessageParserFactory messageParserFactory;

    @Inject
    private transient EventTriggerMatcher eventTriggerMatcher;

    @Inject
    private transient SQSJobFactory sqsJobFactory;

    @Inject
    private transient ExecutorService executor;

    private transient SQSJob sqsJob;

    @DataBoundConstructor
    public SQSTrigger(final String queueUuid, final String subscribedBranches) {
        this.queueUuid = queueUuid;
        this.subscribedBranches = subscribedBranches;
    }

    public Collection<? extends Action> getProjectActions() {
        if (this.job != null) {
            return Arrays.asList(new SQSTriggerActivityAction(this.job));
        }
        return Collections.emptyList();
    }

    private void loadSqsJob() {
        Context.injector().injectMembers(this);
        log.debug("Job is AbstractProject? %s or WorkflowJob? %s", this.job, job instanceof AbstractProject, job instanceof WorkflowJob);
        this.sqsJob = this.sqsJobFactory.createSqsJob(this.job, this);
    }

    @Override
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF")
    public void start(final Job<?, ?> job, final boolean newInstance) {
        super.start(job, newInstance);

        loadSqsJob();

        final DescriptorImpl descriptor = (DescriptorImpl) this.getDescriptor();
        descriptor.queue.execute(new Runnable() {

            @Override
            @SuppressFBWarnings("NP_NULL_PARAM_DEREF")
            public void run() {
                boolean succeed = SQSTrigger.this.scheduler.register(SQSTrigger.this);
                log.info("Register trigger for %s? %s", SQSTrigger.this.job, SQSTrigger.this.getQueueUuid(), succeed);
            }
        });
    }

    @Override
    public void stop() {
        super.stop();

        final DescriptorImpl descriptor = (DescriptorImpl) this.getDescriptor();
        descriptor.queue.execute(new Runnable() {

            @Override
            @SuppressFBWarnings("NP_NULL_PARAM_DEREF")
            public void run() {
                boolean succeed = SQSTrigger.this.scheduler.unregister(SQSTrigger.this);
                log.info("Unregister trigger %s", SQSTrigger.this.job, succeed);
            }
        });
    }

    @Override
    public List<Message> handleMessages(final List<Message> messages) {
        List<Message> proceedMessages = new ArrayList<>();
        for (final Message message : messages) {
            if (this.handleMessage(message)) {
                proceedMessages.add(message);
            }
        }
        return proceedMessages;
    }

    @Override
    public String getQueueUuid() {
        return this.queueUuid;
    }

    @Override
    public String getSubscribedBranches() {
        return this.subscribedBranches;
    }

    @SuppressFBWarnings("NP_NULL_PARAM_DEREF")
    private boolean handleMessage(final Message message) {
        log.info("Parse and do match against events, message: %s", this.job, message.getBody());

        final MessageParser parser = this.messageParserFactory.createParser(message);
        final EventTriggerMatcher matcher = this.eventTriggerMatcher;
        final List<Event> events = parser.parseMessage(message);

        if (matcher.matches(events, this.sqsJob)) {
            log.info("Hurray! Execute it", this.job);
            this.execute(message);
            return true;
        }

        return false;
    }

    @SuppressFBWarnings({"NP_NULL_ON_SOME_PATH", "NP_NULL_PARAM_DEREF"})
    private void execute(final Message message) {
        this.executor.execute(new Runnable() {

            @Override
            @SuppressFBWarnings("NP_NULL_PARAM_DEREF")
            public void run() {
                try {
                    new SQSTriggerBuilder(SQSTrigger.this.sqsJob, message).run();
                } catch (Exception e) {
                    UnexpectedException error = new UnexpectedException(e);
                    SQSTrigger.log.error("Unable to execute job for this message %s, cause: %s", SQSTrigger.this.job, message.getMessageId(), error);
                    throw error;
                }
            }
        });
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    public List<String> getScmRepoUrls() {
        ArrayList<String> scmRepoUrls = new ArrayList<>();
        for (SCM scm : this.sqsJob.getScmList()) {
            scmRepoUrls.addAll(com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.parseScmUrls(scm));
        }
        return scmRepoUrls;
    }

    public void setScheduler(SQSQueueMonitorScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setMessageParserFactory(MessageParserFactory messageParserFactory) {
        this.messageParserFactory = messageParserFactory;
    }

    public void setEventTriggerMatcher(EventTriggerMatcher eventTriggerMatcher) {
        this.eventTriggerMatcher = eventTriggerMatcher;
    }

    public void setSqsJobFactory(SQSJobFactory sqsJobFactory) {
        this.sqsJobFactory = sqsJobFactory;
    }

    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    @Extension
    public static final class DescriptorImpl extends TriggerDescriptor {

        private volatile List<SQSTriggerQueue> sqsQueues;

        private volatile transient Map<String, SQSTriggerQueue> sqsQueueMap;
        private transient boolean isLoaded;

        private transient final SequentialExecutionQueue queue = new SequentialExecutionQueue(Executors.newSingleThreadExecutor());

        public static DescriptorImpl get() {
            final DescriptorExtensionList<Trigger<?>, TriggerDescriptor> triggers = Trigger.all();
            return triggers.get(DescriptorImpl.class);
        }

        public DescriptorImpl() {
            super(SQSTrigger.class);
        }

        @Override
        public synchronized void load() {
            super.load();
            this.initQueueMap();
            this.isLoaded = true;
        }

        @Override
        public boolean isApplicable(final Item item) {
            return item instanceof Job;
        }

        @Override
        public String getDisplayName() {
            return Messages.displayName();
        }

        public ListBoxModel doFillQueueUuidItems() {
            final List<SQSTriggerQueue> queues = this.getSqsQueues();
            final ListBoxModel items = new ListBoxModel();

            for (final SQSTriggerQueue queue : queues) {
                items.add(queue.getName(), queue.getUuid());
            }

            return items;
        }

        public FormValidation doCheckQueueUuid(@QueryParameter final String value) {
            if (this.getSqsQueues().size() == 0) {
                return FormValidation.error(Messages.errorQueueUnavailable());
            }

            if (StringUtils.isEmpty(value)) {
                return FormValidation.ok(Messages.infoQueueDefault());
            }

            final SQSQueue queue = this.getSqsQueue(value);

            if (queue == null) {
                return FormValidation.error(Messages.errorQueueUuidUnknown());
            }

            return FormValidation.ok();
        }

        public FormValidation doCheckSubscribedBranches(@QueryParameter final String subscribedBranches) {
            if (StringUtils.isBlank(subscribedBranches)) {
                return FormValidation.warning(Messages.warningSubscribedBranches());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
            final Object sqsQueues = json.get("sqsQueues");

            this.sqsQueues = req.bindJSONToList(SQSTriggerQueue.class, sqsQueues);
            this.initQueueMap();

            this.save();

            EventBroker.getInstance().post(new ConfigurationChangedEvent());
            return true;
        }

        public List<SQSTriggerQueue> getSqsQueues() {
            if (!this.isLoaded) {
                this.load();
            }
            if (this.sqsQueues == null) {
                return Collections.emptyList();
            }
            return this.sqsQueues;
        }

        public SQSQueue getSqsQueue(final String uuid) {
            if (!this.isLoaded) {
                this.load();
            }
            if (this.sqsQueueMap == null) {
                return null;
            }
            return this.sqsQueueMap.get(uuid);
        }

        private void initQueueMap() {
            if (this.sqsQueues == null) {
                return;
            }

            this.sqsQueueMap = Maps.newHashMapWithExpectedSize(this.sqsQueues.size());

            for (final SQSTriggerQueue queue : this.sqsQueues) {
                this.sqsQueueMap.put(queue.getUuid(), queue);
            }
        }
    }
}
