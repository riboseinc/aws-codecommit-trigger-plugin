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
import lombok.Data;


@Data
public class Record {

//    @Expose
//    @SerializedName("awsRegion")
//    private String awsRegion;

    @Expose
    private CodeCommit codecommit;

//    @Expose
//    @SerializedName("eventId")
//    private String eventId;

//    @Expose
//    @SerializedName("eventName")
//    private String eventName;

//    @Expose
//    @SerializedName("eventPartNumber")
//    private int eventPartNumber;
//
    @Expose
    private String eventSource;

    @Expose
    private String eventSourceARN;

//    @Expose
//    @SerializedName("eventTime")
//    private String eventTime;

//    @Expose
//    @SerializedName("eventTotalParts")
//    private int eventTotalParts;

//    @Expose
//    @SerializedName("eventTriggerConfigId")
//    private String eventTriggerConfigId;

//    @Expose
//    @SerializedName("eventTriggerName")
//    private String eventTriggerName;

//    @Expose
//    @SerializedName("eventVersion")
//    private String eventVersion;

    @Expose
//    @SerializedName("userIdentityARN")
    private String userIdentityARN;
}
