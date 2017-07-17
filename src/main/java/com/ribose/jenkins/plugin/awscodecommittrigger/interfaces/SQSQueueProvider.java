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

import java.util.List;


/**
 * Interface definition for classes that provide access to a collection of {@link SQSQueue}
 * instances.
 */
public interface SQSQueueProvider {

    /**
     * Returns the collection of {@link SQSQueue} instances associated with this provider.
     * @return A collection of {@link SQSQueue} instances.
     */
    List<? extends SQSQueue> getSqsQueues();

    /**
     * Returns the SQS queue configuration associated with the specified identifier.
     * @param uuid The unique identifier of the configuration to get.
     * @return The {@link SQSQueue} associated with the specified identifier, or {@code null} if
     * no such configuration exists.
     */
    SQSQueue getSqsQueue(String uuid);
}
