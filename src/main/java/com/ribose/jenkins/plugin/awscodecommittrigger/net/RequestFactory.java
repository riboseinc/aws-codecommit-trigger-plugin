
package com.ribose.jenkins.plugin.awscodecommittrigger.net;

import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueue;

import java.util.List;


public interface RequestFactory {
    ReceiveMessageRequest createReceiveMessageRequest(final SQSQueue queue);
    ReceiveMessageRequest createReceiveMessageRequest(final String queueUrl, final int maxNumberMessages, final int waitTimeSeconds);
    DeleteMessageBatchRequest createDeleteMessageBatchRequest(final SQSQueue queue, final List<Message> messages);
    DeleteMessageBatchRequest createDeleteMessageBatchRequest(final String queueUrl, final List<Message> messages);
}
