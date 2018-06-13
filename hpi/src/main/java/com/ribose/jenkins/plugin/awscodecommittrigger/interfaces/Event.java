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

import org.eclipse.jgit.transport.URIish;


/**
 * Interface definition for classes that represent source code management (SCM) events posted to an
 * Amazon SQS queue.
 */
public interface Event {

    /**
     * Returns the host of the repository that raised the event.
     * @return The name of the host.
     */
    String getHost();

    /**
     * Returns the path of the repository that raised the event.
     * @return The path of the repository on {@code host}.
     */
    String getPath();

    /**
     * Returns the user that caused the event to be raised.
     * @return The name of the user that caused the event to be raised.
     */
    String getUser();

    /**
     * Returns the branch affected by the changed the caused the event to be raised.
     * @return The name of the branch that caused the event to be raised.
     */
    String getBranch();

    String getNoPrefixBranch();

    /**
     * Returns a value indicating whether the specified URI matches the events host and path
     * information.
     * @param uri The {@link URIish} to be tested.
     * @return {@code true} if the event matches the specified URI; otherwise, {@code false}.
     */
    boolean isMatch(URIish uri);

    String getArn();
}
