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

package com.ribose.jenkins.plugin.awscodecommittrigger.interfaces;

import com.amazonaws.services.sqs.model.Message;
import com.ribose.jenkins.plugin.awscodecommittrigger.net.SQSChannel;


/**
 * Interface definition for classes that can be used to monitor an Amazon {@link SQSQueue} for new
 * {@link Message}s that arrive in the queue.
 */
public interface SQSQueueMonitor extends Runnable {

    /**
     * Returns a new instance for the specified queue and channel that has the same listeners as
     * this instance.
     * @param queue The {@link SQSQueue} to monitor.
     * @param channel The {@link SQSChannel} used to access the queue.
     * @return A new {@link SQSQueueMonitor} instance.
     */
    SQSQueueMonitor clone(SQSQueue queue, SQSChannel channel);

    /**
     * Registers a new listener with the monitor. The listener is notified when new messages arrive
     * in the queue.
     * <p>
     * <b>Note:</b> If a new listener is registered with a monitor that is already processing
     * received messages the listener may not be notified until the next time new messages arrive
     * in the queue.
     * @param listener The {@link SQSQueueListener} to register with the monitor.
     * @return {@code true} if the call caused monitoring to be started (i.e. the first listener was
     * registered); otherwise, {@code false}.
     * @throws IllegalArgumentException The specified listener is {@code null}
     * @throws IllegalArgumentException The specified listener is associated with a different
     * queue.
     */
    boolean add(SQSQueueListener listener);

    /**
     * Unregisters a previously registered listener from the monitor. The listener will no longer
     * be notified when new messages arrive in the queue.
     * @param listener The {@link SQSQueueListener} to unregister from the monitor.
     * @return {@code true} if the call caused monitoring to be stopped (i.e. the last listener was
     * unregistered); otherwise, {@code false}.
     */
    boolean remove(SQSQueueListener listener);

    /**
     * Stops the monitor.
     * <p>
     * Prevents the monitor from making any further requests to the associated queue. Requests that
     * are already in flight may not necessarily be cancelled.
     */
    void shutDown();

    /**
     * Returns a value indicating whether the monitor is stopped.
     * @return {@code true} if the monitor is stopped; otherwise, {@code false}.
     */
    boolean isShutDown();

    /**
     * Returns the SQS queue this monitor is associated with.
     * @return The {@link SQSQueue} this monitor is associated with.
     */
    SQSQueue getQueue();

    /**
     * Returns the SQS channel this monitor is associated with.
     * @return The {@link SQSChannel} this monitor is associated with.
     */
    SQSChannel getChannel();
}
