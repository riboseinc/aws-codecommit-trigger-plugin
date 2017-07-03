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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import io.relution.jenkins.awssqs.i18n.sqstriggerqueue.Messages;
import io.relution.jenkins.awssqs.interfaces.SQSFactory;
import io.relution.jenkins.awssqs.interfaces.SQSQueue;
import io.relution.jenkins.awssqs.logging.Log;
import org.apache.commons.lang3.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class SQSTriggerQueue extends AbstractDescribableImpl<SQSTriggerQueue> implements SQSQueue {

//    public static final Pattern SQS_URL_PATTERN = Pattern
//        .compile("^(?:http(?:s)?://)?(?<endpoint>sqs\\..+?\\.amazonaws\\.com)/(?<id>.+?)/(?<name>.*)$");
//
//    public static final Pattern CODECOMMIT_URL_PATTERN = Pattern
//        .compile("^(?:http(?:s)?://)?git-codecommit\\.(?<region>.+?)\\.amazonaws\\.com/v1/repos/(?<name>.*)$");

//    public static final int WAIT_TIME_SECONDS_DEFAULT = 20;
//    private static final int WAIT_TIME_SECONDS_MIN = 1;
//    private static final int WAIT_TIME_SECONDS_MAX = 20;
//
//    private static final int MAX_NUMBER_OF_MESSAGES_DEFAULT = 10;
//    private static final int MAX_NUMBER_OF_MESSAGES_MIN = 1;
//    private static final int MAX_NUMBER_OF_MESSAGES_MAX = 10;

    private final String uuid;

    //    private final String nameOrUrl;
//    private final String selectUrls;
    private final String accessKey;
    private final Secret secretKey;

    private final Integer waitTimeSeconds;
    private final Integer maxNumberOfMessages;

    private String url;
//    private final String name;
//    private final String endpoint;

    private transient SQSFactory factory;
    private transient AmazonSQS sqs;

    private transient String s;

    @DataBoundConstructor
    public SQSTriggerQueue(
        final String uuid,
        final String url,
        final String accessKey,
        final Secret secretKey,
        final Integer waitTimeSeconds,
        final Integer maxNumberOfMessages) {
        this.uuid = StringUtils.isBlank(uuid) ? UUID.randomUUID().toString() : uuid;
//        this.selectUrls = selectUrls;
//        this.uuid = UUID.randomUUID().toString();
        this.url = url;

        this.accessKey = accessKey;
        this.secretKey = secretKey;
//        this.nameOrUrl = nameOrUrl;

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

//        final Matcher sqsUrlMatcher = SQS_URL_PATTERN.matcher(nameOrUrl);
//
//        if (sqsUrlMatcher.matches()) {
//            this.url = nameOrUrl;
//            this.name = sqsUrlMatcher.group("name");
//            this.endpoint = sqsUrlMatcher.group("endpoint");
//        } else {
//            this.name = nameOrUrl;
//            this.endpoint = null;
//        }
//
//        Descriptor<SQSTriggerQueue> desc = this.getDescriptor();
        Log.info("Create new SQSTriggerQueue(%s, %s)", this.uuid, this.url);
    }

    public AmazonSQS getSQSClient() {
        if (this.sqs == null) {
//            this.sqs = this.getFactory().createSQS(this);
            this.sqs = this.getFactory().createSQSAsync(this);
        }
        return this.sqs;
    }

    @Inject
    public void setFactory(final io.relution.jenkins.awssqs.interfaces.SQSFactory factory) {
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

//    public String getNameOrUrl() {
//        return this.nameOrUrl;
//    }

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
//        if (this.url == null) {
//            final AmazonSQS client = this.getSQSClient();
//            final GetQueueUrlResult result = client.getQueueUrl(this.name);
//            this.url = result.getQueueUrl();
//        }
        return this.url;
    }

    @Override
    public String getName() {
        return plugins.jenkins.awssqs.utils.StringUtils.getSqsQueueName(this.url);
    }

//    @Override
//    public String getEndpoint() {
//        return this.endpoint;
//    }

//    public String getSelectUrls() {
//        return selectUrls;
//    }

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

//    @Override
//    public boolean isValid() {
////        if (StringUtils.isBlank(this.getName())) {
////            return false;
////        }
////        if (StringUtils.isBlank(this.getAWSAccessKeyId())) {
////            return false;
////        }
////        if (StringUtils.isBlank(this.getAWSSecretKey())) {
////            return false;
////        }
//        return true;
//    }

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

//    @Override
//    public String toString() {
//        if (this.s == null) {
//            final StringBuilder sb = new StringBuilder();
//            sb.append(this.name);
//
//            if (!StringUtils.isBlank(this.endpoint)) {
//                sb.append(" (");
//                sb.append(this.endpoint);
//                sb.append(")");
//            }
//
//            sb.append(" {");
//            sb.append(this.uuid);
//            sb.append("}");
//
//            this.s = sb.toString();
//        }
//        return this.s;
//    }

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
        Log.info("no-op method");
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<SQSTriggerQueue> {

        private final transient String selectUrlId = UUID.randomUUID().toString();

        private transient SQSFactory factory;

//        private transient ListQueuesResult listQueuesResult;
//        private Secret secretKey;
//        private List sqsQueues;
//        private List<String> selectUrls = new ArrayList<>();
//        private transient String uuid;

        public DescriptorImpl() {
            super();
            this.factory = Context.injector().getBinding(SQSFactory.class).getProvider().get();
            this.load();
//            this.loadListQueues(this.accessKey, this.secretKey);
        }

//        public SQSFactory getFactory() {
//            if (this.factory == null) {
//                this.factory = Context.injector().getBinding(SQSFactory.class).getProvider().get();
//            }
//            return factory;
//        }

        //        @Override
//        public synchronized void load() {
//            super.load();
//        }

//        @Override
//        protected XmlFile getConfigFile() {
//            return super.getConfigFile();
////            return new XmlFile(new File(Jenkins.getInstance().getRootDir(),getId()+".xml"));
//        }

//
//        public List<String> getSelectUrls() {
//            return selectUrls;
//        }
//
//        public void setSelectUrls(List<String> selectUrls) {
//            this.selectUrls = selectUrls;
//        }

//        public synchronized void save() {
//            super.save();
//        }

//        @Override
//        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
////            json = json.getJSONObject("graven");
////            enableCloud = json.getBoolean("enableCloud");
//            this.save();
//            return super.configure(req, json);
//        }

//        public SQSTriggerQueue newInstance(@CheckForNull StaplerRequest req, @Nonnull JSONObject formData) throws FormException {
//            return super.newInstance(req, formData);
//        }

        @Override
        public String getDisplayName() {
            return Messages.displayName();
        }

//        public String getSelectUrls() {
//            return selectUrls;
//        }

        //        public FormValidation doCheckNameOrUrl(@QueryParameter final String value) {
//            if (StringUtils.isBlank(value)) {
//                return FormValidation.warning(Messages.warningUrl());
//            }
//
//            final Matcher sqsUrlMatcher = SQS_URL_PATTERN.matcher(value);
//
//            if (sqsUrlMatcher.matches()) {
//                final String name = sqsUrlMatcher.group("name");
//                return FormValidation.ok(Messages.infoUrlSqs(), name);
//            }
//
//            final Matcher ccUrlMatcher = CODECOMMIT_URL_PATTERN.matcher(value);
//
//            if (ccUrlMatcher.matches()) {
//                return FormValidation.error(Messages.errorUrlCodecommit());
//            }
//
//            if (StringUtils.startsWith(value, "http://") || StringUtils.startsWith(value, "https://")) {
//                return FormValidation.error(Messages.errorUrlUnknown());
//            }
//
//            return FormValidation.ok();
//        }

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

//        public AutoCompletionCandidates doAutoCompleteQueueUrl(@QueryParameter final String accessKey,
//                                                               @QueryParameter final Secret secretKey) {
//            AutoCompletionCandidates abc = new AutoCompletionCandidates();
//            abc.add(this.selectUrls.toArray(new String[]{}));
//            return abc;
//        }

        public FormValidation doValidate(
//            @QueryParameter final String uuid,
            @QueryParameter final String url,
            @QueryParameter final String accessKey,
            @QueryParameter final Secret secretKey) throws IOException, ServletException {
            try {
//                this.uuid = uuid;
//                final SQSTriggerQueue queue = new SQSTriggerQueue(accessKey, secretKey, 0, 0);

//                if (StringUtils.isBlank(queue.getName())) {
//                    return FormValidation.warning("Name or URL of the queue must be set.");
//                }

//                synchronized (this.validateOk) {
//                    this.validateOk = Boolean.FALSE;
//                }
                if (StringUtils.isBlank(accessKey)) {
                    return FormValidation.warning("AWS access key ID must be set.");
                }

                if (StringUtils.isBlank(secretKey.getPlainText())) {
                    return FormValidation.warning("AWS secret key must be set.");
                }

//                synchronized (isValidated) {
//                    isValidated = Boolean.TRUE;
//                }

                AmazonSQSAsync client = this.factory.createSQSAsync(accessKey, secretKey.getPlainText());
                if (client != null) {
//                    synchronized (this.validateOk) {
//                        this.validateOk = Boolean.TRUE;//TODO remove?
//                    }

//                    DiscoverItems discoverNodes = getDescriptor().mgr.discoverNodes(null); //get all nodes

//                    this.calcFillSettings("url", null);
//                    Jenkins.getInstance().getItem()
                    String queueUrl = client.getQueueUrl(plugins.jenkins.awssqs.utils.StringUtils.getSqsQueueName(url)).getQueueUrl();
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

        public ListBoxModel doFillUrlItems(@QueryParameter final String accessKey, @QueryParameter final Secret secretKey) {
            ListBoxModel items = new ListBoxModel();
            try {
                AmazonSQSAsync client = this.factory.createSQSAsync(accessKey, secretKey.getPlainText());
                List<String> queueUrls = client.listQueues().getQueueUrls();
                for (String queueUrl : queueUrls) {
                    items.add(plugins.jenkins.awssqs.utils.StringUtils.getSqsQueueName(queueUrl), queueUrl);
                }
            } catch (Exception e) {
                items.clear();
            }
            return items;
        }

        public String getSelectUrlId() {
            return this.selectUrlId;
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
