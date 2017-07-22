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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Item;
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import hudson.triggers.Trigger;
import hudson.triggers.TriggerDescriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.SequentialExecutionQueue;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SQSTrigger extends Trigger<AbstractProject<?, ?>> implements SQSQueueListener {
    private static final Log log = Log.get(SQSTrigger.class);

    private final String queueUuid;
    private final String subscribedBranches;

    private transient SQSQueueMonitorScheduler scheduler;

    private transient MessageParserFactory messageParserFactory;
    private transient EventTriggerMatcher eventTriggerMatcher;

    private transient ExecutorService executor;
    private transient List<String> scmRepoUrls;

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

    @Override
    public void start(final AbstractProject<?, ?> project, final boolean newInstance) {
        super.start(project, newInstance);

        final DescriptorImpl descriptor = (DescriptorImpl) this.getDescriptor();
        descriptor.queue.execute(new Runnable() {

            @Override
            public void run() {
                boolean succeed = SQSTrigger.this.getScheduler().register(SQSTrigger.this);
                log.info("Register trigger %s", SQSTrigger.this.job, succeed);
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
                boolean succeed = SQSTrigger.this.getScheduler().unregister(SQSTrigger.this);
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

    @Inject
    public void setScheduler(final SQSQueueMonitorScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public SQSQueueMonitorScheduler getScheduler() {
        if (this.scheduler == null) {
            Context.injector().injectMembers(this);
        }
        return this.scheduler;
    }

    @Inject
    public void setMessageParserFactory(final MessageParserFactory factory) {
        this.messageParserFactory = factory;
    }

    public MessageParserFactory getMessageParserFactory() {
        if (this.messageParserFactory == null) {
            Context.injector().injectMembers(this);
        }
        return this.messageParserFactory;
    }

    @Inject
    public void setEventTriggerMatcher(final EventTriggerMatcher matcher) {
        this.eventTriggerMatcher = matcher;
    }

    public EventTriggerMatcher getEventTriggerMatcher() {
        if (this.eventTriggerMatcher == null) {
            Context.injector().injectMembers(this);
        }
        return this.eventTriggerMatcher;
    }

    @Inject
    public void setExecutorService(final ExecutorService executor) {
        this.executor = executor;
    }

    public ExecutorService getExecutorService() {
        if (this.executor == null) {
            Context.injector().injectMembers(this);
        }
        return this.executor;
    }

    private boolean handleMessage(final Message message) {
        log.info("Parse and do match against events, message body: %s", this.job, message.getBody());

        final MessageParser parser = this.messageParserFactory.createParser(message);
        final EventTriggerMatcher matcher = this.getEventTriggerMatcher();
        final List<Event> events = parser.parseMessage(message);

        if (matcher.matches(events, this.job)) {
            log.info("Hurray! Execute it", this.job);
            this.execute(message);
            return true;
        }

        return false;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    private void execute(final Message message) {
        final SQSTriggerActivityAction activity = SQSTrigger.this.job.getAction(SQSTriggerActivityAction.class);
        activity.logInfo("Submit new thread to handle message '%s' for job '%s'", message.getMessageId(), job.getName());

        this.executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    new SQSTriggerBuilder(SQSTrigger.this.job, message).run();
                } catch (Exception e) {
                    UnexpectedException error = new UnexpectedException(e);
                    activity.logError(error);
                    throw error;
                }
            }
        });
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH")
    public List<String> getScmRepoUrls() {
        if (this.scmRepoUrls == null) {
            this.scmRepoUrls = new ArrayList<>();

            SCM scm = this.job.getScm();
            if (scm instanceof GitSCM) {
                final GitSCM git = (GitSCM) this.job.getScm();
                List<RemoteConfig> repos = git.getRepositories();

                for (RemoteConfig repo : repos) {
                    List<URIish> uris = repo.getURIs();
                    for (URIish uri : uris) {
                        this.scmRepoUrls.add(uri.toASCIIString());
                    }
                }
            }
        }

        return this.scmRepoUrls;
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
            return item instanceof AbstractProject;
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
