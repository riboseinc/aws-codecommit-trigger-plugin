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
import com.amazonaws.PredefinedClientConfigurations;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.buffered.AmazonSQSBufferedAsyncClient;
import com.amazonaws.services.sqs.buffered.QueueBufferConfig;
import com.google.inject.Inject;
import hudson.ProxyConfiguration;
import io.relution.jenkins.awssqs.interfaces.SQSExecutorFactory;
import io.relution.jenkins.awssqs.interfaces.SQSFactory;
import io.relution.jenkins.awssqs.interfaces.SQSQueue;
import io.relution.jenkins.awssqs.interfaces.SQSQueueMonitor;
import io.relution.jenkins.awssqs.net.RequestFactory;
import io.relution.jenkins.awssqs.net.SQSChannel;
import io.relution.jenkins.awssqs.net.SQSChannelImpl;
import io.relution.jenkins.awssqs.threading.SQSQueueMonitorImpl;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.ExecutorService;

public class SQSFactoryImpl implements SQSFactory {

    private final RequestFactory factory;
    private final SQSExecutorFactory SQSExecutorFactory;

    @Inject
    public SQSFactoryImpl(final SQSExecutorFactory SQSExecutorFactory, final RequestFactory factory) {
        this.SQSExecutorFactory = SQSExecutorFactory;
        this.factory = factory;
    }

    @Override
    public AmazonSQS createSQSAsync(final SQSQueue queue) {
        ClientConfiguration clientConfiguration = this.getClientConfiguration(queue);
        AmazonSQSAsyncClientBuilder sqsAsyncBuilder = AmazonSQSAsyncClientBuilder.standard()
            .withClientConfiguration(clientConfiguration)
            .withCredentials(queue.hasCredentials() ? queue : DefaultAWSCredentialsProviderChain.getInstance())
            .withExecutorFactory(this.SQSExecutorFactory);

        final QueueBufferConfig queueBufferConfig = this.getQueueBufferConfig(queue);
        final AmazonSQSBufferedAsyncClient sqsBufferedAsync = new AmazonSQSBufferedAsyncClient(sqsAsyncBuilder.build(), queueBufferConfig);

        return sqsBufferedAsync;
    }


    @Override
    public AmazonSQS createSQSAsync(String accessKey, String secretKey) {
        ClientConfiguration clientConfiguration = this.getClientConfiguration(null);
        AmazonSQSAsyncClientBuilder sqsAsyncBuilder = AmazonSQSAsyncClientBuilder.standard()
            .withClientConfiguration(clientConfiguration)
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .withExecutorFactory(this.SQSExecutorFactory);

        final QueueBufferConfig queueBufferConfig = this.getQueueBufferConfig(null);
        final AmazonSQSBufferedAsyncClient sqsBufferedAsync = new AmazonSQSBufferedAsyncClient(sqsAsyncBuilder.build(), queueBufferConfig);

        return sqsBufferedAsync;
    }

    private SQSChannel createChannel(final SQSQueue queue) {
        final AmazonSQS sqs = this.createSQSAsync(queue);
        return new SQSChannelImpl(sqs, queue, this.factory);
    }

    @Override
    public SQSQueueMonitor createMonitor(final ExecutorService executor, final SQSQueue queue) {
        final AmazonSQS sqs = this.createSQSAsync(queue);
        final SQSChannel channel = new SQSChannelImpl(sqs, queue, this.factory);
        return new SQSQueueMonitorImpl(executor, queue, channel);
    }

    @Override
    public SQSQueueMonitor createMonitor(final SQSQueueMonitor monitor, final SQSQueue queue) {
        final SQSChannel channel = this.createChannel(queue);
        return monitor.clone(queue, channel);
    }

    //@param queue might be null
    private ClientConfiguration getClientConfiguration(final SQSQueue queue) {
        ClientConfiguration config = PredefinedClientConfigurations.defaultConfig();

        Jenkins jenkins = Jenkins.getInstance();
        if (jenkins == null) {
            return config;
        }

        //TODO
        ProxyConfiguration proxyConfig = jenkins.proxy;
        Proxy proxy = proxyConfig == null ?
            Proxy.NO_PROXY :
            proxyConfig.createProxy( queue == null ? "sqs.*.amazonaws.com" : plugins.jenkins.awssqs.utils.StringUtils.getSqsEndpoint(queue.getUrl()));
        if (!proxy.equals(Proxy.NO_PROXY) && proxy.address() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) proxy.address();
            config.setProxyHost(address.getHostName());
            config.setProxyPort(address.getPort());
//            config.setNonProxyHosts("169.254.169.254");//TODO

            if (StringUtils.isNotBlank(proxyConfig.getUserName())) {
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

    //@param queue might be null
    private QueueBufferConfig getQueueBufferConfig(final SQSQueue queue) {
        final QueueBufferConfig config = new QueueBufferConfig();

        // TODO Add more options
        config.setLongPollWaitTimeoutSeconds(queue == null ? SQSQueue.WAIT_TIME_SECONDS_DEFAULT : queue.getWaitTimeSeconds());
        config.setLongPoll(true);

        return config;
    }
}
