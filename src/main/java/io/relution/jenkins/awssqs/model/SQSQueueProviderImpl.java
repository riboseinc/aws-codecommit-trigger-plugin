
package io.relution.jenkins.awssqs.model;

import java.util.List;

import io.relution.jenkins.awssqs.interfaces.SQSQueueProvider;


public class SQSQueueProviderImpl implements SQSQueueProvider {

    @Override
    public List<? extends io.relution.jenkins.awssqs.interfaces.SQSQueue> getSqsQueues() {
        final io.relution.jenkins.awssqs.SQSTrigger.DescriptorImpl descriptor = io.relution.jenkins.awssqs.SQSTrigger.DescriptorImpl.get();
        return descriptor.getSqsQueues();
    }

    @Override
    public io.relution.jenkins.awssqs.interfaces.SQSQueue getSqsQueue(final String uuid) {
        final io.relution.jenkins.awssqs.SQSTrigger.DescriptorImpl descriptor = io.relution.jenkins.awssqs.SQSTrigger.DescriptorImpl.get();
        return descriptor.getSqsQueue(uuid);
    }
}
