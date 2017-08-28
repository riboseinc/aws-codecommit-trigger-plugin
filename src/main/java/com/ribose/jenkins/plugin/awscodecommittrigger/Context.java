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

package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.ribose.jenkins.plugin.awscodecommittrigger.factories.*;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.*;
import com.ribose.jenkins.plugin.awscodecommittrigger.matchers.EventTriggerMatcherImpl;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.SQSQueueProviderImpl;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJobFactory;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.impl.SQSJobFactoryImpl;
import com.ribose.jenkins.plugin.awscodecommittrigger.net.RequestFactory;
import com.ribose.jenkins.plugin.awscodecommittrigger.net.RequestFactoryImpl;
import com.ribose.jenkins.plugin.awscodecommittrigger.threading.ExecutorProviderImpl;
import com.ribose.jenkins.plugin.awscodecommittrigger.threading.SQSQueueMonitorSchedulerImpl;
import jenkins.model.Jenkins;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;


public class Context extends com.google.inject.AbstractModule {

    private static Injector injector;

    public synchronized static Injector injector() {
        Jenkins jenkins = Jenkins.getInstance();//TODO optimize this code
        if (jenkins != null) {
            InternalInjector internalInjector = jenkins.lookup.setIfNull(InternalInjector.class, new InternalInjector());
            injector = internalInjector.resolve();
        }

        if (injector == null) {
            injector = Guice.createInjector(new Context());
        }
        return injector;
    }

    @Override
    protected void configure() {
        this.bind(ThreadFactory.class)
            .to(ThreadFactoryImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(SQSExecutorFactory.class)
            .to(SQSExecutorFactoryImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(ExecutorProvider.class)
            .to(ExecutorProviderImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(ExecutorService.class)
            .toProvider(ExecutorProvider.class)
            .in(com.google.inject.Singleton.class);

        this.bind(SQSFactory.class)
            .to(SQSFactoryImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(RequestFactory.class)
            .to(RequestFactoryImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(SQSQueueProvider.class)
            .to(SQSQueueProviderImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(SQSQueueMonitorScheduler.class)
            .to(SQSQueueMonitorSchedulerImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(MessageParserFactory.class)
            .to(MessageParserFactoryImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(EventTriggerMatcher.class)
            .to(EventTriggerMatcherImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(SQSJobFactory.class)
            .to(SQSJobFactoryImpl.class)
            .in(com.google.inject.Singleton.class);

        this.bind(ScmFactory.class)
            .to(ScmFactoryImpl.class)
            .in(com.google.inject.Singleton.class);
    }
}
