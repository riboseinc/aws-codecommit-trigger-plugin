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

package com.ribose.jenkins.plugin.awscodecommittrigger.threading;

import com.amazonaws.services.sqs.model.Message;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueueListener;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueueMonitor;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.net.SQSChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;


public class SQSQueueMonitorImpl implements SQSQueueMonitor {

    private final static Log log = Log.get(SQSQueueMonitorImpl.class);
    private final ExecutorService executor;

    private final SQSQueue queue;
    private final SQSChannel channel;

    private final Object listenersLock = new Object();
    private final List<SQSQueueListener> listeners;

    private final AtomicBoolean isRunning = new AtomicBoolean();
    private volatile boolean isShutDown;

    public SQSQueueMonitorImpl(final ExecutorService executor, final SQSQueue queue, final SQSChannel channel) {
        this.executor = executor;
        this.queue = queue;
        this.channel = channel;
        this.listeners = new ArrayList<>();
    }

    private SQSQueueMonitorImpl(final ExecutorService executor, final SQSQueue queue, final SQSChannel channel, final List<SQSQueueListener> listeners) {
        this.executor = executor;
        this.queue = queue;
        this.channel = channel;
        this.listeners = listeners;
    }

    @Override
    public SQSQueueMonitor clone(final SQSQueue queue, final SQSChannel channel) {
        synchronized (this.listenersLock) {
            return new SQSQueueMonitorImpl(this.executor, queue, channel, this.listeners);
        }
    }

    @Override
    public boolean add(final SQSQueueListener listener) {
        assert listener.getQueueUuid().equals(this.channel.getQueueUuid());

        synchronized (this.listenersLock) {
            if (this.listeners.add(listener) && this.listeners.size() == 1) {
                this.isShutDown = false;
                this.execute();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(final SQSQueueListener listener) {
        if (listener == null) {
            return false;
        }

        synchronized (this.listenersLock) {
            if (this.listeners.remove(listener) && this.listeners.isEmpty()) {
                this.shutDown();
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            if (this.isShutDown) {
                return;
            }

            if (!this.isRunning.compareAndSet(false, true)) {
                log.warning("Monitor for %s already started", this.queue);
                return;
            }

            log.debug("Start monitor for %s", this.queue);
            this.processMessages();
        } catch (Exception e) {
            log.warning("Monitor for %s stopped, error: %s", this.queue, e);
            this.isShutDown = true;
        } finally {
            if (!this.isRunning.compareAndSet(true, false)) {
                log.warning("Monitor for %s already stopped", this.queue);
            }
            this.execute();
        }
    }

    @Override
    public void shutDown() {
        log.debug("Shut down monitor for %s", this.channel);
        this.isShutDown = true;
    }

    @Override
    public boolean isShutDown() {
        return this.isShutDown;
    }

    @Override
    public SQSQueue getQueue() {
        return this.queue;
    }

    @Override
    public SQSChannel getChannel() {
        return this.channel;
    }

    private void execute() {
        if (!this.isShutDown) {
            this.executor.execute(this);
        }
    }

    private void processMessages() {
        if (this.isShutDown) {
            return;
        }

        final List<Message> messages = this.channel.getMessages();
        List<Message> proceedMessages = notifyListeners(messages);
        log.debug("Received %d messages, proceed %d messages", messages.size(), proceedMessages.size());
        this.channel.deleteMessages(messages);
    }

    private List<Message> notifyListeners(final List<Message> messages) {
        List<Message> proceedMessages = new ArrayList<>();

        if (!messages.isEmpty()) {
            final List<SQSQueueListener> listeners = this.getListeners();
            for (final SQSQueueListener listener : listeners) {
                List<Message> msgs = listener.handleMessages(messages);
                proceedMessages.addAll(msgs);
            }
        }

        return proceedMessages;
    }

    private List<SQSQueueListener> getListeners() {
        synchronized (this.listenersLock) {
            return new ArrayList<>(this.listeners);
        }
    }
}
