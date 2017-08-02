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

package com.ribose.jenkins.plugin.awscodecommittrigger.threading;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.*;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.events.ConfigurationChangedEvent;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.events.EventBroker;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;


public class SQSQueueMonitorSchedulerImpl implements SQSQueueMonitorScheduler {
    private static final Log log = Log.get(SQSQueueMonitorSchedulerImpl.class);

    private final ExecutorService executor;
    private final SQSQueueProvider provider;
    private SQSFactory factory;

    private final Map<String, SQSQueueMonitor> monitors = new HashMap<>();

    @Inject
    public SQSQueueMonitorSchedulerImpl(final ExecutorService executor, final SQSQueueProvider provider, final SQSFactory factory) {
        this.executor = executor;
        this.provider = provider;
        this.factory = factory;

        EventBroker.getInstance().register(this);
    }

    @Override
    public boolean register(final SQSQueueListener listener) {
        log.info("Register SQS listener");
        final String uuid = listener.getQueueUuid();
        final SQSQueue queue = this.provider.getSqsQueue(uuid);

        if (queue == null) {
            log.warning("No queue for {%s}, aborted", uuid);
            return false;
        }

        this.register(listener, uuid, queue);
        return true;
    }

    @Override
    public synchronized boolean unregister(final SQSQueueListener listener) {
        if (listener == null) {
            return false;
        }

        log.info("Unregister SQS listener");
        final String uuid = listener.getQueueUuid();
        final SQSQueueMonitor monitor = this.monitors.get(uuid);

        if (monitor == null) {
            log.warning("No monitor for {%s}, aborted", uuid);
            return false;
        }

        log.info("Remove listener from monitor for {%s}", uuid);
        if (monitor.remove(listener)) {
            monitor.shutDown();
        }

        if (monitor.isShutDown()) {
            log.debug("Monitor is shut down, remove monitor for {%s}", uuid);
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

    private synchronized void register(final SQSQueueListener listener, final String uuid, final SQSQueue queue) {
        SQSQueueMonitor monitor = this.monitors.get(uuid);

        if (monitor == null) {
            log.info("No monitor exists, creating new monitor for %s", queue);
            monitor = this.factory.createMonitor(this.executor, queue);
            this.monitors.put(uuid, monitor);
        }

        log.debug("Add listener to monitor for %s", queue);
        monitor.add(listener);
    }

    private void reconfigure(final Iterator<Entry<String, SQSQueueMonitor>> entries, final Entry<String, SQSQueueMonitor> entry) {
        final String uuid = entry.getKey();
        SQSQueueMonitor monitor = entry.getValue();
        final SQSQueue queue = this.provider.getSqsQueue(uuid);

        if (queue == null) {
            log.info("Queue {%s} removed, shut down monitor", uuid);
            monitor.shutDown();
            entries.remove();
        } else if (monitor.isShutDown() || this.hasQueueChanged(monitor, queue)) {
            log.info("Queue {%s} changed or monitor stopped, create new monitor", uuid);
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
        } catch (Exception e) {
            log.warning("Cannot compare queues: %s, error: %s", e.getMessage(), e);
        }
        return true;
    }

    public synchronized void setFactory(SQSFactory factory) {
        this.factory = factory;
    }
}
