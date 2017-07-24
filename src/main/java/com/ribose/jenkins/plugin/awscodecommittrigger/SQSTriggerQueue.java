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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.google.inject.Inject;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSFactory;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueue;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import com.ribose.jenkins.plugin.awscodecommittrigger.i18n.sqstriggerqueue.Messages;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class SQSTriggerQueue extends AbstractDescribableImpl<SQSTriggerQueue> implements SQSQueue {
    private static final Log log = Log.get(SQSTriggerQueue.class);

    private final String uuid;
    private final String accessKey;
    private final Secret secretKey;
    private final Integer waitTimeSeconds;
    private final Integer maxNumberOfMessages;
    private final String url;

    private transient SQSFactory factory;
    private transient AmazonSQS sqs;

    @DataBoundConstructor
    public SQSTriggerQueue(
        final String uuid,
        final String url,
        final String accessKey,
        final Secret secretKey,
        final Integer waitTimeSeconds,
        final Integer maxNumberOfMessages) {
        this.uuid = StringUtils.isBlank(uuid) ? UUID.randomUUID().toString() : uuid;
        this.url = url;

        this.accessKey = accessKey;
        this.secretKey = secretKey;

        this.waitTimeSeconds = this.limit(
            waitTimeSeconds,
            WAIT_TIME_SECONDS_MIN,
            WAIT_TIME_SECONDS_MAX,
            WAIT_TIME_SECONDS_DEFAULT);

        this.maxNumberOfMessages = this.limit(
            maxNumberOfMessages,
            MAX_NUMBER_OF_MESSAGES_MIN,
            MAX_NUMBER_OF_MESSAGES_MAX,
            MAX_NUMBER_OF_MESSAGES_DEFAULT);

        log.debug("Create new SQSTriggerQueue(%s, %s)", this.uuid, this.url);
    }

    public AmazonSQS getSQSClient() {
        if (this.sqs == null) {
            this.sqs = this.getFactory().createSQSAsync(this);
        }
        return this.sqs;
    }

    @Inject
    public void setFactory(final SQSFactory factory) {
        this.factory = factory;
    }

    public SQSFactory getFactory() {
        if (this.factory == null) {
            Context.injector().injectMembers(this);
        }
        return this.factory;
    }

    @Override
    public String getUuid() {
        return this.uuid;
    }

    public String getAccessKey() {
        return this.accessKey;
    }

    public Secret getSecretKey() {
        return this.secretKey;
    }

    @Override
    public int getWaitTimeSeconds() {
        if (this.waitTimeSeconds == null) {
            return WAIT_TIME_SECONDS_DEFAULT;
        }
        return this.waitTimeSeconds;
    }

    @Override
    public int getMaxNumberOfMessages() {
        if (this.maxNumberOfMessages == null) {
            return MAX_NUMBER_OF_MESSAGES_DEFAULT;
        }
        return this.maxNumberOfMessages;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public String getName() {
        return com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.getSqsQueueName(this.url);
    }

    @Override
    public String getAWSAccessKeyId() {
        return this.accessKey;
    }

    @Override
    public String getAWSSecretKey() {
        if (this.secretKey == null) {
            return null;
        }
        return this.secretKey.getPlainText();
    }

    @Override
    public boolean hasCredentials() {
        return StringUtils.isNotBlank(this.getAWSAccessKeyId()) && StringUtils.isNotBlank(this.getAWSSecretKey());
    }

    @Override
    public int hashCode() {
        return this.uuid.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof SQSTriggerQueue)) {
            return false;
        }

        final SQSTriggerQueue other = (SQSTriggerQueue) obj;
        if (!this.uuid.equals(other.uuid)) {
            return false;
        }

        return true;
    }

    //TODO review this function
    private int limit(final Integer value, final int min, final int max, final int fallbackValue) {
        if (value == null || value < min || value > max) {
            return fallbackValue;
        } else {
            return value;
        }
    }

    @Override
    public AWSCredentials getCredentials() {
        return this;
    }

    @Override
    public void refresh() {
        log.info("no-op method");
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SQSTriggerQueue> {

        private transient SQSFactory factory;

        public DescriptorImpl() {
            super();
            this.factory = Context.injector().getBinding(SQSFactory.class).getProvider().get();
            this.load();
        }

        @Override
        public String getDisplayName() {
            return Messages.displayName();
        }


        public FormValidation doCheckWaitTimeSeconds(@QueryParameter final String value) {
            return this.validateNumber(
                value,
                WAIT_TIME_SECONDS_MIN,
                WAIT_TIME_SECONDS_MAX,
                Messages.errorWaitTimeSeconds());
        }

        public FormValidation doCheckMaxNumberOfMessage(@QueryParameter final String value) {
            return this.validateNumber(
                value,
                MAX_NUMBER_OF_MESSAGES_MIN,
                MAX_NUMBER_OF_MESSAGES_MAX,
                Messages.errorMaxNumberOfMessages());
        }

        public FormValidation doValidate(@QueryParameter final String url, @QueryParameter final String accessKey, @QueryParameter final Secret secretKey) throws IOException, ServletException {
            try {
                if (StringUtils.isBlank(accessKey)) {
                    return FormValidation.warning("AWS access key ID must be set.");
                }

                if (StringUtils.isBlank(secretKey.getPlainText())) {
                    return FormValidation.warning("AWS secret key must be set.");
                }

                AmazonSQS client = this.factory.createSQSAsync(accessKey, secretKey.getPlainText());
                if (client != null) {
                    String queueUrl = client.getQueueUrl(com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.getSqsQueueName(url)).getQueueUrl();
                    if (queueUrl.equalsIgnoreCase(url)) {
                        return FormValidation.ok("Access to SQS successful");
                    }
                }

                return FormValidation.error("Failed to access to SQS");
            } catch (final AmazonServiceException ase) {
                return FormValidation.error(ase, ase.getMessage());
            } catch (final Exception ex) {
                return FormValidation.error(ex, "Error validating SQS access");
            }
        }

        public void setFactory(SQSFactory factory) {
            this.factory = factory;
        }

        public ListBoxModel doFillUrlItems(@QueryParameter final String accessKey, @QueryParameter final Secret secretKey) {
            ListBoxModel items = new ListBoxModel();
            try {
                AmazonSQS client = this.factory.createSQSAsync(accessKey, secretKey.getPlainText());
                List<String> queueUrls = client.listQueues().getQueueUrls();
                for (String queueUrl : queueUrls) {
                    items.add(com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils.getSqsQueueName(queueUrl), queueUrl);
                }
            } catch (Exception e) {
                items.clear();
            }
            return items;
        }

        private FormValidation validateNumber(final String value, final int min, final int max, final String message) {
            try {
                if (StringUtils.isBlank(value)) {
                    return FormValidation.error(message);
                }

                final int number = Integer.parseInt(value);

                if (number < min || number > max) {
                    return FormValidation.error(message);
                }

                return FormValidation.ok();

            } catch (final NumberFormatException e) {
                return FormValidation.error(message);
            }
        }
    }
}
