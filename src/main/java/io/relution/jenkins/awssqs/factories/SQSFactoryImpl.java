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

package io.relution.jenkins.awssqs.factories;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClient;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.buffered.QueueBufferConfig;
import com.google.inject.Inject;

import hudson.ProxyConfiguration;
import io.relution.jenkins.awssqs.net.SQSChannel;
import jenkins.model.Jenkins;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.ExecutorService;

import static org.apache.commons.lang3.StringUtils.isNotBlank;


public class SQSFactoryImpl implements io.relution.jenkins.awssqs.interfaces.SQSFactory {

    private final ExecutorService executor;
    private final io.relution.jenkins.awssqs.net.RequestFactory factory;

    @Inject
    public SQSFactoryImpl(final ExecutorService executor, final io.relution.jenkins.awssqs.net.RequestFactory factory) {
        this.executor = executor;
        this.factory = factory;
    }

    @Override
    public AmazonSQS createSQS(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue) {
        final ClientConfiguration clientConfiguration = this.getClientConfiguration(queue);
        boolean hasCredentials = isNotBlank(queue.getAWSAccessKeyId()) && isNotBlank(queue.getAWSSecretKey());
        io.relution.jenkins.awssqs.logging.Log.info("Creating AmazonSQS instance - hasCredentials='%s'", hasCredentials);
        final AmazonSQS sqs = hasCredentials ? new AmazonSQSClient(queue, clientConfiguration) : new AmazonSQSClient(clientConfiguration);

        if (queue.getEndpoint() != null) {
            sqs.setEndpoint(queue.getEndpoint());
        }

        return sqs;
    }

    @Override
    public AmazonSQSAsync createSQSAsync(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue) {
        final ClientConfiguration clientConfiguration = this.getClientConfiguration(queue);
        boolean hasCredentials = isNotBlank(queue.getAWSAccessKeyId()) && isNotBlank(queue.getAWSSecretKey());
        io.relution.jenkins.awssqs.logging.Log.info("Creating AmazonSQS instance - hasCredentials='%s'", hasCredentials);
        final AmazonSQSAsyncClient sqsAsync = hasCredentials ? new AmazonSQSAsyncClient(queue, clientConfiguration, this.executor) : new AmazonSQSAsyncClient(clientConfiguration);

        if (queue.getEndpoint() != null) {
            sqsAsync.setEndpoint(queue.getEndpoint());
        }

        final QueueBufferConfig queueBufferConfig = this.getQueueBufferConfig(queue);
        final AmazonSQSBufferedAsyncClient sqsBufferedAsync = new AmazonSQSBufferedAsyncClient(sqsAsync, queueBufferConfig);

        return sqsBufferedAsync;
    }

    @Override
    public SQSChannel createChannel(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue) {
        final AmazonSQS sqs = this.createSQS(queue);
        return new io.relution.jenkins.awssqs.net.SQSChannelImpl(sqs, queue, this.factory);
    }

    @Override
    public io.relution.jenkins.awssqs.interfaces.SQSQueueMonitor createMonitor(final ExecutorService executor, final io.relution.jenkins.awssqs.interfaces.SQSQueue queue) {
        final SQSChannel channel = this.createChannel(queue);
        return new io.relution.jenkins.awssqs.threading.SQSQueueMonitorImpl(executor, queue, channel);
    }

    @Override
    public io.relution.jenkins.awssqs.interfaces.SQSQueueMonitor createMonitor(final io.relution.jenkins.awssqs.interfaces.SQSQueueMonitor monitor, final io.relution.jenkins.awssqs.interfaces.SQSQueue queue) {
        final SQSChannel channel = this.createChannel(queue);
        return monitor.clone(queue, channel);
    }

    private ClientConfiguration getClientConfiguration(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue) {
        final ClientConfiguration config = new ClientConfiguration();

        // Check to see if Jenkins is up yet
        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            return config;
        }
        ProxyConfiguration proxyConfig = jenkins.proxy;
        Proxy proxy = proxyConfig == null ? Proxy.NO_PROXY : proxyConfig.createProxy(queue.getEndpoint());
        if (!proxy.equals(Proxy.NO_PROXY) && proxy.address() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) proxy.address();
            config.setProxyHost(address.getHostName());
            config.setProxyPort(address.getPort());
            config.setNonProxyHosts("169.254.169.254");
            if (null != proxyConfig.getUserName()) {
                config.setProxyUsername(proxyConfig.getUserName());
                config.setProxyPassword(proxyConfig.getPassword());
            }
            io.relution.jenkins.awssqs.logging.Log.info(
                "Proxy settings for SQS: %s:%s",
                config.getProxyHost(),
                config.getProxyPort());
        }
        return config;
    }

    private QueueBufferConfig getQueueBufferConfig(final io.relution.jenkins.awssqs.interfaces.SQSQueue queue) {
        final QueueBufferConfig config = new QueueBufferConfig();

        // TODO Add more options

        config.setLongPollWaitTimeoutSeconds(queue.getWaitTimeSeconds());
        config.setLongPoll(true);

        return config;
    }
}
