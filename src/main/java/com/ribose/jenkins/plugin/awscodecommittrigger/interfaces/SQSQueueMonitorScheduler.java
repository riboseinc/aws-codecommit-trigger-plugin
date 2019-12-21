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

import com.google.common.eventbus.Subscribe;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.events.ConfigurationChangedEvent;


/**
 * Interface definition for classes that schedule the execution of {@link SQSQueueMonitor}
 * instances.
 */
public interface SQSQueueMonitorScheduler {

    /**
     * Registers the specified listener with the scheduler. The listener is notified when new
     * messages arrive in the associated queue.
     * <p>
     * The listener may be registered with a new monitor or an existing instance at the scheduler's
     * discretion.
     * @param listener The {@link SQSQueueListener} to be registered.
     * @return {@code true} if the listener was registered. {@code false} if no queue configuration
     * associated with the listener could be found.
     * @throws IllegalArgumentException The specified listener is {@code null}.
     */
    boolean register(SQSQueueListener listener);

    /**
     * Unregisters the specified listener from the scheduler. The listener will no longer
     * be notified of new messages.
     * @param listener The {@link SQSQueueListener} to be unregistered.
     * @return {@code true} if the listener was unregistered. {@code false} if the specified
     * listener is not associated with a monitor.
     */
    boolean unregister(SQSQueueListener listener);

    /**
     * Notifies the scheduler that the global configuration was changed. It should shut down all
     * monitors for which the associated queue configuration was removed.
     * @param event The configuration changed event.
     */
    @Subscribe
    void onConfigurationChanged(ConfigurationChangedEvent event);
}
