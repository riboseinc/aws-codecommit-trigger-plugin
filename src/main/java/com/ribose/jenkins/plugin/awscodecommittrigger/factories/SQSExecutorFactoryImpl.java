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

package com.ribose.jenkins.plugin.awscodecommittrigger.factories;

import com.google.inject.Inject;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSExecutorFactory;

import java.util.concurrent.*;


public class SQSExecutorFactoryImpl implements SQSExecutorFactory {

    /**
     * The number of threads to start by default. Cannot exceed the maximum number of threads.
     * Beware: If you reduce this number and do not place a limit on the queue no additional
     * threads will ever be started.
     */
    private final static int      CORE_POOL_SIZE       = 10;
    private final static int      MAXIMUM_POOL_SIZE    = 50;

    private final static int      KEEP_ALIVE_TIME      = 5;
    private final static TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MINUTES;

    final ThreadFactory           threadFactory;

    @Inject
    public SQSExecutorFactoryImpl(final ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public ExecutorService newExecutor() {
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_TIME,
            KEEP_ALIVE_TIME_UNIT,
            new LinkedBlockingQueue<Runnable>(),
            this.threadFactory);

        executor.allowCoreThreadTimeOut(false);
        return executor;
    }
}
