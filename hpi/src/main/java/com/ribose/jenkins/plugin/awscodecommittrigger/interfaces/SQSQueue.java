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

import com.amazonaws.regions.Regions;
import com.ribose.jenkins.plugin.awscodecommittrigger.credentials.AwsCredentials;


/**
 * Interface definition for classes that represent the necessary configuration that is required to
 * access an Amazon SQS queue.
 */
public interface SQSQueue {

    int WAIT_TIME_SECONDS_DEFAULT = 20;
    int WAIT_TIME_SECONDS_MIN = 1;
    int WAIT_TIME_SECONDS_MAX = 20;

    int MAX_NUMBER_OF_MESSAGES_DEFAULT = 10;
    int MAX_NUMBER_OF_MESSAGES_MIN = 1;
    int MAX_NUMBER_OF_MESSAGES_MAX = 10;

    /**
     * Returns the identifier used to uniquely identify the queue configuration.
     * @return The unique identifier of this configuration.
     */
    String getUuid();

    /**
     * Returns the ER of the queue the configuration is associated with.
     * @return The ER of a queue.
     */
    String getUrl();

    /**
     * Returns the name of the queue the configuration is associated with.
     * @return The name of a queue.
     */
    String getName();


    /**
     * Returns the time, in seconds, requests should wait for new messages to arrive in the queue.
     * @return The wait time, in seconds, before a receive message request should time out.
     */
    int getWaitTimeSeconds();

    /**
     * Returns the maximum number of messages that a request should request.
     * @return The maximum number of messages a receive message request should request from the
     * queue.
     */
    int getMaxNumberOfMessages();

    boolean hasCredentials();

    Regions getRegion();

    String getCredentialsId();

    AwsCredentials lookupAwsCredentials();
}
