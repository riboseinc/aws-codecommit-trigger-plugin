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

package io.relution.jenkins.awssqs.net;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;

import org.apache.commons.httpclient.HttpStatus;

import java.util.Collections;
import java.util.List;

import io.relution.jenkins.awssqs.interfaces.SQSQueue;
import io.relution.jenkins.awssqs.logging.Log;
import io.relution.jenkins.awssqs.model.constants.ErrorCode;
import io.relution.jenkins.awssqs.util.ErrorType;
import io.relution.jenkins.awssqs.util.ThrowIf;


public class SQSChannelImpl implements SQSChannel {

    private final AmazonSQS      sqs;
    private final SQSQueue       queue;
    private final io.relution.jenkins.awssqs.net.RequestFactory factory;

    /**
     * Number of requests that were sent (for logging)
     */
    private int                  requestCount;

    public SQSChannelImpl(final AmazonSQS sqs, final SQSQueue queue, final io.relution.jenkins.awssqs.net.RequestFactory factory) {
        ThrowIf.isNull(sqs, "sqs");
        ThrowIf.isNull(queue, "queue");
        ThrowIf.isNull(factory, "factory");

        this.sqs = sqs;
        this.queue = queue;
        this.factory = factory;
    }

    @Override
    public List<Message> getMessages() {
        try {
            this.logRequestCount();

            final ReceiveMessageRequest request = this.factory.createReceiveMessageRequest(this.queue);
            final ReceiveMessageResult result = this.sqs.receiveMessage(request);

            if (result == null) {
                return Collections.emptyList();
            }

            return result.getMessages();

        } catch (final com.amazonaws.services.sqs.model.QueueDoesNotExistException e) {
            Log.warning("Failed to send receive message request for %s, queue does not exist", this.queue);
            throw e;

        } catch (final com.amazonaws.AmazonServiceException e) {
            if (ErrorType.is(e, ErrorCode.INVALID_CLIENT_TOKEN_ID, HttpStatus.SC_FORBIDDEN)) {
                Log.warning("Failed to send receive message request for %s, %s", this.queue, e.getMessage());
                throw e;
            }

            Log.severe(e, "Failed to send receive message request for %s", this.queue);
        }
        return Collections.emptyList();
    }

    @Override
    public void deleteMessages(final List<Message> messages) {
        if (messages == null || messages.size() == 0) {
            return;
        }

        final DeleteMessageBatchResult result = this.deleteMessageBatch(messages);

        if (result == null) {
            return;
        }

        final List<?> failed = result.getFailed();
        final List<?> success = result.getSuccessful();
        Log.info("Deleted %d message(s) (%d failed) from %s", success.size(), failed.size(), this.queue);
    }

    @Override
    public String getQueueUuid() {
        return this.queue.getUuid();
    }

    @Override
    public String toString() {
        return this.queue.toString();
    }

    private void logRequestCount() {
        this.requestCount++;
        Log.fine("Send receive message request #%d for %s", this.requestCount, this.queue);
    }

    private DeleteMessageBatchResult deleteMessageBatch(final List<Message> messages) {
        try {
            final DeleteMessageBatchRequest request = this.factory.createDeleteMessageBatchRequest(this.queue, messages);
            Log.info("Send delete request for %d message(s) to %s", messages.size(), this.queue);
            return this.sqs.deleteMessageBatch(request);

        } catch (final com.amazonaws.AmazonServiceException e) {
            Log.severe(e, "Delete from %s failed", this.queue);

        }
        return null;
    }
}
