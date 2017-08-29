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

import com.google.inject.Inject;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.ExecutorProvider;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSExecutorFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;


public class ExecutorProviderImpl implements ExecutorProvider {

    private final ThreadPoolExecutor executor;

    @Inject
    public ExecutorProviderImpl(final SQSExecutorFactory factory) {
        this.executor = (ThreadPoolExecutor) factory.newExecutor();
    }

    @Override
    public int getCorePoolSize() {
        return this.executor.getCorePoolSize();
    }

    @Override
    public void setCorePoolSize(final int corePoolSize) throws IllegalArgumentException {
        this.executor.setCorePoolSize(corePoolSize);
    }

    @Override
    public ExecutorService get() {
        return this.executor;
    }
}
