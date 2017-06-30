
package io.relution.jenkins.awssqs.net;

import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.util.ArrayList;
import java.util.List;


public class RequestFactoryImpl implements RequestFactory {

    @Override
    public ReceiveMessageRequest createReceiveMessageRequest(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue) {
        final ReceiveMessageRequest request = new ReceiveMessageRequest(queue.getUrl());
        request.setMaxNumberOfMessages(queue.getMaxNumberOfMessages());
        request.setWaitTimeSeconds(queue.getWaitTimeSeconds());
        return request;
    }

    @Override
    public DeleteMessageBatchRequest createDeleteMessageBatchRequest(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue, final List<Message> messages) {
        final List<DeleteMessageBatchRequestEntry> entries = new ArrayList<>(messages.size());

        for (final Message message : messages) {
            final DeleteMessageBatchRequestEntry entry = this.createDeleteMessageBatchRequestEntry(message);
            entries.add(entry);
        }

        final DeleteMessageBatchRequest request = new DeleteMessageBatchRequest(queue.getUrl());
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
