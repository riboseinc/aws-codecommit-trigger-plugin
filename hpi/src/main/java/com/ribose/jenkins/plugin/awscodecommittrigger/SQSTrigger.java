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
import com.cloudbees.plugins.credentials.Credentials;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.ribose.jenkins.plugin.awscodecommittrigger.credentials.StandardAwsCredentials;
import com.ribose.jenkins.plugin.awscodecommittrigger.exception.UnexpectedException;
import com.ribose.jenkins.plugin.awscodecommittrigger.i18n.sqstrigger.Messages;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.*;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.events.ConfigurationChangedEvent;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.events.EventBroker;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.RepoInfo;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJobFactory;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Item;
import hudson.model.Job;
import hudson.security.AccessDeniedException2;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import hudson.util.SequentialExecutionQueue;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SQSTrigger extends Trigger<Job<?, ?>> implements SQSQueueListener {

    private static final Log log = Log.get(SQSTrigger.class);

    private String queueUuid;
    private List<SQSScmConfig> sqsScmConfigs;
    private boolean subscribeInternalScm;

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
    private transient List<SQSActivityAction> actions;

    @DataBoundConstructor
    public SQSTrigger(final String queueUuid, boolean subscribeInternalScm, final List<SQSScmConfig> sqsScmConfigs) {
        this.queueUuid = queueUuid;
        this.sqsScmConfigs = sqsScmConfigs;
        this.subscribeInternalScm = subscribeInternalScm;
    }

    public Collection<? extends Action> getProjectActions() {
        if (this.job != null && CollectionUtils.isEmpty(this.actions)) {
            this.actions = Collections.singletonList(new SQSActivityAction(this.job));
        }

        if (this.actions == null) {
            this.actions = Collections.emptyList();
        }
        return this.actions;
    }

    private void loadSqsJob() {
        Context.injector().injectMembers(this);
        log.debug("Job is AbstractProject? %s or WorkflowJob? %s", this.job, job instanceof AbstractProject, job instanceof WorkflowJob);
        this.sqsJob = this.sqsJobFactory.createSqsJob(this.job, this);
    }

    @Override
    public void start(@Nonnull final Job<?, ?> job, final boolean newInstance) {
        super.start(job, newInstance);

        loadSqsJob();

        final DescriptorImpl descriptor = (DescriptorImpl) this.getDescriptor();
        descriptor.queue.execute(new Runnable() {

            @Override
            public void run() {
                boolean succeed = SQSTrigger.this.scheduler.register(SQSTrigger.this);
                log.debug("Register trigger for %s? %s", SQSTrigger.this.job, SQSTrigger.this.getQueueUuid(), succeed);
            }
        });
    }

    @Override
    public void stop() {
        super.stop();

        final DescriptorImpl descriptor = (DescriptorImpl) this.getDescriptor();
        descriptor.queue.execute(new Runnable() {

            @Override
            public void run() {
                boolean succeed = SQSTrigger.this.scheduler.unregister(SQSTrigger.this);
                log.debug("Unregister trigger %s", SQSTrigger.this.job, succeed);
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

    @CheckForNull
    public List<SQSScmConfig> getSqsScmConfigs() {
        return sqsScmConfigs;
    }

    public boolean isSubscribeInternalScm() {
        return subscribeInternalScm;
    }

    private boolean handleMessage(final Message message) {
        log.debug("Parse and do match against events, message: %s", this.job, message.getBody());

        final MessageParser parser = this.messageParserFactory.createParser(message);
        final EventTriggerMatcher matcher = this.eventTriggerMatcher;
        final List<Event> events = parser.parseMessage(message);

        boolean matched = matcher.matches(events, this.sqsJob);
        String messageId = com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.getMessageId(message);
        log.info("Any event matched? %s. Message: %s", this.job, matched, messageId);
        if (matched) {
            log.debug("Hurray! Execute it", this.job);
            this.execute(message);
            return true;
        }

        return false;
    }

    private void execute(@Nonnull final Message message) {
        this.executor.execute(new Runnable() {

            @Override
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

    public boolean isWorkflowJob() {
        return this.job instanceof WorkflowJob;
    }

    public String getJobName() {
        assert this.job != null;
        return this.job.getName();
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

    public void setSqsScmConfigs(List<SQSScmConfig> sqsScmConfigs) {
        this.sqsScmConfigs = sqsScmConfigs;
    }

    public void setSubscribeInternalScm(boolean subscribeInternalScm) {
        this.subscribeInternalScm = subscribeInternalScm;
    }

    public void setSqsJob(SQSJob sqsJob) {
        this.sqsJob = sqsJob;
    }

    public void setQueueUuid(String queueUuid) {
        this.queueUuid = queueUuid;
    }

    public void setActions(List<SQSActivityAction> actions) {
        this.actions = actions;
    }

    @Extension
    public static final class DescriptorImpl extends TriggerDescriptor {

        private volatile List<SQSTriggerQueue> sqsQueues;
        private volatile transient Map<String, SQSTriggerQueue> sqsQueueMap;

        private transient boolean isLoaded;
        private transient final SequentialExecutionQueue queue = new SequentialExecutionQueue(Executors.newSingleThreadExecutor());
        private transient SQSJobFactory sqsJobFactory;

        public DescriptorImpl() {
            super(SQSTrigger.class);
            this.sqsJobFactory = Context.injector().getBinding(SQSJobFactory.class).getProvider().get();
        }

        @Override
        public Trigger newInstance(StaplerRequest req, @Nonnull JSONObject jsonObject) throws FormException {
            if (jsonObject.has("subscribeInternalScm")) {
                jsonObject.put("subscribeInternalScm", Boolean.TRUE);
            }
            return super.newInstance(req, jsonObject);
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

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.displayName();
        }

        public RepoInfo getRepoInfo(Job job) {
            SQSJob sqsJob = this.sqsJobFactory.createSqsJob(job, null);
            return RepoInfo.fromSqsJob(sqsJob);
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

        @Override
        public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
            Object sqsQueues = json.get("sqsQueues");
            if (json.size() == 1) {
                String key = json.keys().next().toString();
                sqsQueues = json.getJSONObject(key).get("sqsQueues");
            }
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

            for (SQSTriggerQueue sqsQueue : this.sqsQueues) {
                String version = sqsQueue.getVersion();
                boolean compatible =  com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.checkCompatibility(version,  com.ribose.jenkins.plugin.awscodecommittrigger.PluginInfo.compatibleSinceVersion);
                sqsQueue.setCompatible(compatible);
            }

            this.sqsQueueMap = Maps.newHashMapWithExpectedSize(this.sqsQueues.size());

            for (final SQSTriggerQueue queue : this.sqsQueues) {
                this.sqsQueueMap.put(queue.getUuid(), queue);
            }
        }

        public boolean checkCompatible() {
            if (!this.isLoaded) {
                this.load();
            }

            if (this.sqsQueues == null) {
                return true;
            }

            for (SQSTriggerQueue sqsQueue : this.sqsQueues) {
                if (!sqsQueue.isCompatible()) {
                    return false;
                }
            }
            return true;
        }

        public FormValidation doMigration() {
            try {
                Jenkins.getActiveInstance().checkPermission(CredentialsProvider.CREATE);
            }
            catch (AccessDeniedException2 e){
                return FormValidation.error("No Permission to Create new Credentials in the System");
            }

            SystemCredentialsProvider provider = SystemCredentialsProvider.getInstance();
            List<Credentials> globalCredentials = provider.getDomainCredentialsMap().get(Domain.global());
            int originalSize = globalCredentials.size();

            for (SQSTriggerQueue sqsQueue : this.sqsQueues) {
                if (!sqsQueue.isCompatible()) {
                    final String accessKey = sqsQueue.getAccessKey();
                    final Secret secretKey = sqsQueue.getSecretKey();

                    StandardAwsCredentials credential = (StandardAwsCredentials) CollectionUtils.find(globalCredentials, new Predicate() {

                        @Override
                        public boolean evaluate(Object o) {
                            if (!StandardAwsCredentials.class.isInstance(o)) {
                                return false;
                            }

                            StandardAwsCredentials c = StandardAwsCredentials.class.cast(o);
                            return c.getAccessKey().equals(accessKey) && c.getSecretKey().equals(secretKey);
                        }
                    });

                    if (credential == null) {
                        credential = new StandardAwsCredentials("imported", accessKey, secretKey);
                        globalCredentials.add(credential);
                    }

                    sqsQueue.setCredentialsId(credential.getId());
                    sqsQueue.setVersion(com.ribose.jenkins.plugin.awscodecommittrigger.PluginInfo.version);
                }
            }

            if (originalSize < globalCredentials.size()) {//save new credentials added
                try {
                    provider.save();
                } catch (IOException e) {
                    return FormValidation.error("Unable to create credentials in Global Scope");
                }
            }

            this.save();
            this.load();
            EventBroker.getInstance().post(new ConfigurationChangedEvent());

            log.info("Migration successful");
            return FormValidation.ok("Imported successful, click here to refresh the page");
        }
    }
}
