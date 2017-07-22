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

package com.ribose.jenkins.plugin.awscodecommittrigger.model.entities.codecommit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class MessageBody {

    @Expose
    @SerializedName("Type")
    private String type;

    @Expose
    @SerializedName("MessageId")
    private String messageId;

    @Expose
    @SerializedName("TopicArn")
    private String topicArn;

    @Expose
    @SerializedName("Subject")
    private String subject;

    @Expose
    @SerializedName("Message")
    private String message;

    @Expose
    @SerializedName("Timestamp")
    private String timestamp;

    @Expose
    @SerializedName("SignatureVersion")
    private String signatureVersion;

    @Expose
    @SerializedName("Signature")
    private String signature;

    @Expose
    @SerializedName("SigningCertURL")
    private String signingCertURL;

    @Expose
    @SerializedName("UnsubscribeURL")
    private String unsubscribeURL;

    public String getType() {
        return this.type;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public String getTopicArn() {
        return this.topicArn;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getMessage() {
        return this.message;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getSignatureVersion() {
        return this.signatureVersion;
    }

    public String getSignature() {
        return this.signature;
    }

    public String getSigningCertURL() {
        return this.signingCertURL;
    }

    public String getUnsubscribeURL() {
        return this.unsubscribeURL;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setTopicArn(String topicArn) {
        this.topicArn = topicArn;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSignatureVersion(String signatureVersion) {
        this.signatureVersion = signatureVersion;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public void setSigningCertURL(String signingCertURL) {
        this.signingCertURL = signingCertURL;
    }

    public void setUnsubscribeURL(String unsubscribeURL) {
        this.unsubscribeURL = unsubscribeURL;
    }
}
