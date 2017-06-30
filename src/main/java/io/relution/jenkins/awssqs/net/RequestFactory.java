
package io.relution.jenkins.awssqs.net;

import com.amazonaws.services.sqs.model.DeleteMessageBatchRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import java.util.List;


public interface RequestFactory {

    /**
     * Returns a new request that can be used to receive messages from the specified queue.
     * @param queue The {@link io.relution.jenkins.awssqs.interfaces.SQSQueue} for which to create the request.
     * @return A {@link ReceiveMessageRequest} that can be used to request messages from the
     * specified queue.
     */
    ReceiveMessageRequest createReceiveMessageRequest(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue);

    /**
     * Returns a new request that can be used to delete previously received messages from the
     * specified queue.
     * <p>
     * The specified messages must have been received by a previous receive message request to
     * the same queue.
     * @param queue The {@link io.relution.jenkins.awssqs.interfaces.SQSQueue} from which to delete the specified messages.
     * @param messages The collection of {@link Message}s to delete.
     * @return A {@link DeleteMessageBatchRequest} that can be used to delete messages from the
     * specified queue.
     */
    DeleteMessageBatchRequest createDeleteMessageBatchRequest(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue, final List<Message> messages);
}
