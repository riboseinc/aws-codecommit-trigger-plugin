/*
 * Copyright 2017 Ribose Inc. <https://www.ribose.com>
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

package com.ribose.jenkins.plugin.awscodecommittrigger.it.mock;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.*;

import java.util.Collections;

public class MockSQSClient extends AmazonSQSClient {

    private String queueUrl;

    public MockSQSClient(AWSCredentials credentials) {
        super(credentials);
    }

    @Override
    public GetQueueUrlResult getQueueUrl(String queueName) {
        GetQueueUrlResult result = new GetQueueUrlResult();
        result.setQueueUrl(queueUrl);
        return result;
    }

    @Override
    public ListQueuesResult listQueues() {
        return new ListQueuesResult().withQueueUrls(this.queueUrl);
    }

    @Override
    public DeleteMessageBatchResult deleteMessageBatch(DeleteMessageBatchRequest request) {
        return new DeleteMessageBatchResult().withSuccessful(Collections.<DeleteMessageBatchResultEntry>emptyList());
    }

    public String getQueueUrl() {
        return queueUrl;
    }

    public void setQueueUrl(String queueUrl) {
        this.queueUrl = queueUrl;
    }
}
