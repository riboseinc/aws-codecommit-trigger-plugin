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

package io.relution.jenkins.awssqs.model.entities.codecommit;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class Record {

    @Expose
    @SerializedName("awsRegion")
    private String     awsRegion;

    @Expose
    @SerializedName("codecommit")
    private CodeCommit codeCommit;

    @Expose
    @SerializedName("eventId")
    private String     eventId;

    @Expose
    @SerializedName("eventName")
    private String     eventName;

    @Expose
    @SerializedName("eventPartNumber")
    private int        eventPartNumber;

    @Expose
    @SerializedName("eventSource")
    private String     eventSource;

    @Expose
    @SerializedName("eventSourceARN")
    private String     eventSourceARN;

    @Expose
    @SerializedName("eventTime")
    private String     eventTime;

    @Expose
    @SerializedName("eventTotalParts")
    private int        eventTotalParts;

    @Expose
    @SerializedName("eventTriggerConfigId")
    private String     eventTriggerConfigId;

    @Expose
    @SerializedName("eventTriggerName")
    private String     eventTriggerName;

    @Expose
    @SerializedName("eventVersion")
    private String     eventVersion;

    @Expose
    @SerializedName("userIdentityARN")
    private String     userIdentityARN;

    public String getAwsRegion() {
        return this.awsRegion;
    }

    public CodeCommit getCodeCommit() {
        return this.codeCommit;
    }

    public String getEventId() {
        return this.eventId;
    }

    public String getEventName() {
        return this.eventName;
    }

    public int getEventPartNumber() {
        return this.eventPartNumber;
    }

    public String getEventSource() {
        return this.eventSource;
    }

    public String getEventSourceARN() {
        return this.eventSourceARN;
    }

    public String getEventTime() {
        return this.eventTime;
    }

    public int getEventTotalParts() {
        return this.eventTotalParts;
    }

    public String getEventTriggerConfigId() {
        return this.eventTriggerConfigId;
    }

    public String getEventTriggerName() {
        return this.eventTriggerName;
    }

    public String getEventVersion() {
        return this.eventVersion;
    }

    public String getUserIdentityARN() {
        return this.userIdentityARN;
    }
}
