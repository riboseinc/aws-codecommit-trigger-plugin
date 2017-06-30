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

package io.relution.jenkins.awssqs.threading;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;

import io.relution.jenkins.awssqs.interfaces.SQSFactory;
import io.relution.jenkins.awssqs.interfaces.SQSQueue;
import io.relution.jenkins.awssqs.interfaces.SQSQueueMonitor;
import io.relution.jenkins.awssqs.interfaces.SQSQueueMonitorScheduler;
import io.relution.jenkins.awssqs.interfaces.SQSQueueProvider;
import io.relution.jenkins.awssqs.logging.Log;
import io.relution.jenkins.awssqs.model.events.ConfigurationChangedEvent;
import io.relution.jenkins.awssqs.model.events.EventBroker;
import io.relution.jenkins.awssqs.util.ThrowIf;


public class SQSQueueMonitorSchedulerImpl implements SQSQueueMonitorScheduler {

    private final ExecutorService              executor;
    private final SQSQueueProvider             provider;
    private final SQSFactory                   factory;

    private final Map<String, SQSQueueMonitor> monitors = new HashMap<>();

    @Inject
    public SQSQueueMonitorSchedulerImpl(final ExecutorService executor, final SQSQueueProvider provider, final SQSFactory factory) {
        this.executor = executor;
        this.provider = provider;
        this.factory = factory;

        EventBroker.getInstance().register(this);
    }

    @Override
    public boolean register(final io.relution.jenkins.awssqs.interfaces.SQSQueueListener listener) {
        ThrowIf.isNull(listener, "listener");

        Log.info("Register SQS listener");
        final String uuid = listener.getQueueUuid();
        final SQSQueue queue = this.provider.getSqsQueue(uuid);

        if (queue == null) {
            Log.warning("No queue for {%s}, aborted", uuid);
            return false;
        }

        this.register(listener, uuid, queue);
        return true;
    }

    @Override
    public synchronized boolean unregister(final io.relution.jenkins.awssqs.interfaces.SQSQueueListener listener) {
        if (listener == null) {
            return false;
        }

        Log.info("Unregister SQS listener");
        final String uuid = listener.getQueueUuid();
        final SQSQueueMonitor monitor = this.monitors.get(uuid);

        if (monitor == null) {
            Log.warning("No monitor for {%s}, aborted", uuid);
            return false;
        }

        Log.info("Remove listener from monitor for {%s}", uuid);
        if (monitor.remove(listener)) {
            monitor.shutDown();
        }

        if (monitor.isShutDown()) {
            Log.info("Monitor is shut down, remove monitor for {%s}", uuid);
            this.monitors.remove(uuid);
        }

        return true;
    }

    @Override
    @Subscribe
    public synchronized void onConfigurationChanged(final ConfigurationChangedEvent event) {
        final Iterator<Entry<String, SQSQueueMonitor>> entries = this.monitors.entrySet().iterator();

        while (entries.hasNext()) {
            final Entry<String, SQSQueueMonitor> entry = entries.next();
            this.reconfigure(entries, entry);
        }
    }

    private synchronized void register(final io.relution.jenkins.awssqs.interfaces.SQSQueueListener listener, final String uuid, final SQSQueue queue) {
        SQSQueueMonitor monitor = this.monitors.get(uuid);

        if (monitor == null) {
            Log.info("No monitor exists, creating new monitor for %s", queue);
            monitor = this.factory.createMonitor(this.executor, queue);
            this.monitors.put(uuid, monitor);
        }

        Log.info("Add listener to monitor for %s", queue);
        monitor.add(listener);
    }

    private void reconfigure(final Iterator<Entry<String, SQSQueueMonitor>> entries, final Entry<String, SQSQueueMonitor> entry) {
        final String uuid = entry.getKey();
        SQSQueueMonitor monitor = entry.getValue();
        final SQSQueue queue = this.provider.getSqsQueue(uuid);

        if (queue == null) {
            Log.info("Queue {%s} removed, shut down monitor", uuid);
            monitor.shutDown();
            entries.remove();
        } else if (monitor.isShutDown() || this.hasQueueChanged(monitor, queue)) {
            Log.info("Queue {%s} changed or monitor stopped, create new monitor", uuid);
            monitor = this.factory.createMonitor(monitor, queue);
            entry.setValue(monitor).shutDown();
            this.executor.execute(monitor);
        }
    }

    private boolean hasQueueChanged(final SQSQueueMonitor monitor, final SQSQueue queue) {
        try {
            final SQSQueue current = monitor.getQueue();

            if (!StringUtils.equals(current.getUrl(), queue.getUrl())) {
                return true;
            }

            if (!StringUtils.equals(current.getAWSAccessKeyId(), queue.getAWSAccessKeyId())) {
                return true;
            }

            if (!StringUtils.equals(current.getAWSSecretKey(), queue.getAWSSecretKey())) {
                return true;
            }

            if (current.getMaxNumberOfMessages() != queue.getMaxNumberOfMessages()) {
                return true;
            }

            if (current.getWaitTimeSeconds() != queue.getWaitTimeSeconds()) {
                return true;
            }

            return false;
        } catch (final com.amazonaws.AmazonServiceException e) {
            Log.warning("Cannot compare queues: %s", e.getMessage());
        } catch (final Exception e) {
            Log.severe(e, "Cannot compare queues, unknown error");
        }
        return true;
    }
}
