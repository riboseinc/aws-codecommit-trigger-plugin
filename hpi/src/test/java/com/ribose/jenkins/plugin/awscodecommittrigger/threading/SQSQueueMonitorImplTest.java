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

import shaded.com.amazonaws.services.sqs.model.Message;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueueListener;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.SQSQueueMonitor;
import com.ribose.jenkins.plugin.awscodecommittrigger.net.SQSChannel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;


public class SQSQueueMonitorImplTest {

    private static final String UUID_A = "uuid-a";
    private static final String UUID_B = "uuid-b";

    @Mock
    private ExecutorService executor;

    @Mock
    private SQSQueue queue;

    @Mock
    private SQSChannel channel;

    @Mock
    private SQSQueueListener listener;

    private SQSQueueMonitor monitor;

    private final List<Message> messages = new ArrayList<>();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);

        final Message message = new Message();
        this.messages.add(message);

        Mockito.when(this.channel.getMessages()).thenReturn(this.messages);

        Mockito.when(this.listener.getQueueUuid()).thenReturn(UUID_A);
        Mockito.when(this.channel.getQueueUuid()).thenReturn(UUID_A);

        this.monitor = new SQSQueueMonitorImpl(this.executor, this.queue, this.channel);
    }

    @Test
    public void shouldNotThrowIfUnregisterNullListener() {
        assertThat(this.monitor.remove(null)).isFalse();
        assertThat(this.monitor.isShutDown()).isFalse();
    }

    @Test
    public void shouldNotThrowIfUnregisterUnknown() {
        assertThat(this.monitor.remove(this.listener)).isFalse();
        assertThat(this.monitor.isShutDown()).isFalse();
    }

    @Test
    public void shouldStartMonitorForFirstListener() {
        final boolean result = this.monitor.add(this.listener);

        assertThat(result).isTrue();
        Mockito.verify(this.executor).execute(this.monitor);
        assertThat(this.monitor.isShutDown()).isFalse();
    }

    @Test
    public void shouldNotStartMultipleTimesForMultipleListeners() {
        this.monitor.add(this.listener);

        final boolean result = this.monitor.add(this.listener);

        assertThat(result).isFalse();
        Mockito.verify(this.executor).execute(this.monitor);
        assertThat(this.monitor.isShutDown()).isFalse();
    }

    @Test
    public void shouldNotStopBeforeLastListenerRemoved() {
        this.monitor.add(this.listener);
        this.monitor.add(this.listener);
        Mockito.verify(this.executor).execute(this.monitor);

        final boolean result = this.monitor.remove(this.listener);

        assertThat(result).isFalse();
        Mockito.verifyNoMoreInteractions(this.executor);
        assertThat(this.monitor.isShutDown()).isFalse();
    }

    @Test
    public void shouldStopIfLastListenerRemoved() {
        this.monitor.add(this.listener);
        this.monitor.add(this.listener);
        this.monitor.remove(this.listener);
        Mockito.verify(this.executor).execute(this.monitor);

        final boolean result = this.monitor.remove(this.listener);

        assertThat(result).isTrue();
        Mockito.verifyNoMoreInteractions(this.executor);
        assertThat(this.monitor.isShutDown()).isTrue();
    }

    @Test
    public void shouldQueryQueue() {
        this.monitor.add(this.listener);
        Mockito.verify(this.channel).getQueueUuid();
        Mockito.verify(this.listener).getQueueUuid();
        Mockito.verify(this.executor).execute(this.monitor);

        this.monitor.run();

        Mockito.verify(this.channel).getMessages();
        Mockito.verify(this.listener).handleMessages(this.messages);
        Mockito.verifyNoMoreInteractions(this.listener);
        Mockito.verify(this.executor, Mockito.times(2)).execute(this.monitor);
    }

    @Test
    public void shouldNotSendDeleteRequestIfResultIsEmpty() {
        final List<Message> messages = Collections.emptyList();
        Mockito.when(this.channel.getMessages()).thenReturn(messages);
        assertThat(this.monitor.add(this.listener)).isTrue();
        Mockito.verify(this.channel).getQueueUuid();
        Mockito.verify(this.listener).getQueueUuid();
        Mockito.verify(this.executor).execute(this.monitor);

        this.monitor.run();

        Mockito.verify(this.channel).getMessages();
        Mockito.verifyNoMoreInteractions(this.listener);
        Mockito.verify(this.executor, Mockito.times(2)).execute(this.monitor);
    }

    @Test
    public void shouldNotRunIfAlreadyShutDown() {
        this.monitor.add(this.listener);
        Mockito.verify(this.channel).getQueueUuid();
        Mockito.verify(this.listener).getQueueUuid();
        Mockito.verify(this.executor).execute(this.monitor);

        this.monitor.shutDown();
        this.monitor.run();

        assertThat(this.monitor.isShutDown()).isTrue();
        Mockito.verifyNoMoreInteractions(this.channel);
        Mockito.verifyNoMoreInteractions(this.listener);
        Mockito.verifyNoMoreInteractions(this.executor);
    }
}
