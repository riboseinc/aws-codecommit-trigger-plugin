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

import com.google.inject.Provider;

import java.util.concurrent.ExecutorService;


/**
 * Interface definition for classes that provide access to an {@link ExecutorService} instance.
 */
public interface ExecutorProvider extends Provider<ExecutorService> {

    /**
     * Returns the core number of threads.
     * @return The core number of threads.
     * @see #setCorePoolSize(int)
     */
    int getCorePoolSize();

    /**
     * Sets the core number of threads. This overrides any value set in the constructor. If the new
     * value is smaller than the current value, excess existing threads will be terminated when they
     * next become idle. If larger, new threads will, if needed, be started to execute any queued
     * tasks.
     * @param corePoolSize The new core size to set.
     * @throws IllegalArgumentException If {@code corePoolSize} is less than zero.
     * @see #getCorePoolSize()
     */
    void setCorePoolSize(int corePoolSize) throws IllegalArgumentException;
}
