/*
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

package com.ribose.jenkins.plugin.awscodecommittrigger.net;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class SQSChannelImpl implements SQSChannel {

    private static final Log log = Log.get(SQSChannelImpl.class);

    private final AmazonSQS sqs;
    private final SQSQueue queue;
    private final RequestFactory factory;

    /**
     * Number of requests that were sent (for logging)
     */
    private final AtomicInteger requestCount = new AtomicInteger(0);

    public SQSChannelImpl(final AmazonSQS sqs, final SQSQueue queue, final RequestFactory factory) {
        this.sqs = sqs;
        this.queue = queue;
        this.factory = factory;
    }

    @Override
    public List<Message> getMessages() {
        log.info("Star polling messages");

        List<Message> messages = Collections.emptyList();

        try {
            log.debug("Requests count %d for %s", this.requestCount.incrementAndGet(), this.queue);

            final ReceiveMessageRequest request = this.factory.createReceiveMessageRequest(this.queue);
            final ReceiveMessageResult result = this.sqs.receiveMessage(request);
            log.debug("Send request to receive messages from queue %s", this.queue);
            if (result != null) {
                messages = result.getMessages();
            }
        } catch (AmazonServiceException e) {
            log.error("Poll request error", e);
            throw e;
        }

        return messages;
    }

    @Override
    public void deleteMessages(final List<Message> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }

        final DeleteMessageBatchResult result = this.deleteMessageBatch(messages);

        if (result == null) {
            return;
        }

        final List<?> failed = result.getFailed();
        final List<?> success = result.getSuccessful();
        log.info("Delete %d message(s) (%d failed) from %s", success.size(), failed.size(), this.queue);
    }

    @Override
    public String getQueueUuid() {
        return this.queue.getUuid();
    }

    private DeleteMessageBatchResult deleteMessageBatch(final List<Message> messages) {
        try {
            final DeleteMessageBatchRequest request = this.factory.createDeleteMessageBatchRequest(this.queue, messages);
            log.debug("Send request to delete messages from queue %s", this.queue);
            return this.sqs.deleteMessageBatch(request);
        } catch (AmazonServiceException e) {
            log.warning("Unable delete messages from queue %s, error: %s", this.queue, e);
        }
        return null;
    }
}
