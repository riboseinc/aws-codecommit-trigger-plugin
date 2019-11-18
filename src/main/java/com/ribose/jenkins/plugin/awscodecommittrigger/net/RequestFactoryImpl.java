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

package com.ribose.jenkins.plugin.awscodecommittrigger.net;

import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueue;

import java.util.ArrayList;
import java.util.List;


public class RequestFactoryImpl implements RequestFactory {

    @Override
    public ReceiveMessageRequest createReceiveMessageRequest(final SQSQueue queue) {
        return createReceiveMessageRequest(queue.getUrl(), queue.getMaxNumberOfMessages(), queue.getWaitTimeSeconds());
    }

    @Override
    public ReceiveMessageRequest createReceiveMessageRequest(String queueUrl, final int maxNumberMessages, final int waitTimeSeconds) {
        final ReceiveMessageRequest request = new ReceiveMessageRequest(queueUrl);
        request.setMaxNumberOfMessages(maxNumberMessages);
        request.setWaitTimeSeconds(waitTimeSeconds);
        return request;
    }

    @Override
    public DeleteMessageBatchRequest createDeleteMessageBatchRequest(final SQSQueue queue, final List<Message> messages) {
        return createDeleteMessageBatchRequest(queue.getUrl(), messages);
    }

    @Override
    public DeleteMessageBatchRequest createDeleteMessageBatchRequest(String queueUrl, List<Message> messages) {
        final List<DeleteMessageBatchRequestEntry> entries = new ArrayList<>(messages.size());

        for (final Message message : messages) {
            final DeleteMessageBatchRequestEntry entry = this.createDeleteMessageBatchRequestEntry(message);
            entries.add(entry);
        }

        final DeleteMessageBatchRequest request = new DeleteMessageBatchRequest(queueUrl);
        request.setEntries(entries);
        return request;
    }

    private DeleteMessageBatchRequestEntry createDeleteMessageBatchRequestEntry(final Message message) {
        final DeleteMessageBatchRequestEntry entry = new DeleteMessageBatchRequestEntry();
        entry.setReceiptHandle(message.getReceiptHandle());
        entry.setId(message.getMessageId());
        return entry;
    }
}
