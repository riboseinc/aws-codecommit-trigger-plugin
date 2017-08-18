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

package com.ribose.jenkins.plugin.awscodecommittrigger.net;

public class SQSQueueImplTest {

//    @Mock
//    private RequestFactory factory;
//
//    @Mock
//    private AmazonSQS           sqs;
//
//    @Mock
//    private SQSQueue queue;
//
//    private SQSChannel channel;
//
//    private final List<Message> messages = Collections.singletonList(new Message());
//
//    @Before
//    public void init() {
//        MockitoAnnotations.initMocks(this);
//
//        this.channel = new SQSChannelImpl(this.sqs, this.queue, this.factory);
//    }
//
//    @Test
//    public void shouldReturnMessages() {
//        final ReceiveMessageRequest request = Mockito.mock(ReceiveMessageRequest.class);
//        final ReceiveMessageResult result = Mockito.mock(ReceiveMessageResult.class);
//
//        Mockito.when(this.factory.createReceiveMessageRequest(this.queue)).thenReturn(request);
//        Mockito.when(this.sqs.receiveMessage(request)).thenReturn(result);
//        Mockito.when(result.getMessages()).thenReturn(this.messages);
//
//        final List<Message> messages = this.channel.getMessages();
//
//        assertThat(messages).isSameAs(this.messages);
//    }
//
//    @Test
//    public void shouldDeleteMessages() {
//        final DeleteMessageBatchRequest request = Mockito.mock(DeleteMessageBatchRequest.class);
//        final DeleteMessageBatchResult result = Mockito.mock(DeleteMessageBatchResult.class);
//
//        Mockito.when(this.factory.createDeleteMessageBatchRequest(this.queue, this.messages)).thenReturn(request);
//        Mockito.when(this.sqs.deleteMessageBatch(request)).thenReturn(result);
//        Mockito.when(result.getSuccessful()).thenReturn(new ArrayList<DeleteMessageBatchResultEntry>());
//        Mockito.when(result.getFailed()).thenReturn(new ArrayList<BatchResultErrorEntry>());
//
//        this.channel.deleteMessages(this.messages);
//
//        Mockito.verify(this.factory).createDeleteMessageBatchRequest(this.queue, this.messages);
//        Mockito.verify(this.sqs).deleteMessageBatch(request);
//    }
}
