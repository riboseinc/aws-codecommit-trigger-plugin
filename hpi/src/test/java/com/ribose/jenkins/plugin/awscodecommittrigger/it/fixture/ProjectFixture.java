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

package com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSScmConfig;
import hudson.scm.SCM;
import hudson.util.OneShotEvent;

import java.util.List;


public class ProjectFixture implements MarkIndex {
    private static final Long TIMEOUT = 90_000L;//in milliseconds, e.g: 300_000 ~ 5 mins

    private List<SQSScmConfig> scmConfigs;
    private boolean subscribeInternalScm;
    private SCM scm;

    private String pipelineScript;

    private String[] sendBranches;
    private boolean shouldStarted;
    private Long timeout = TIMEOUT;
    private OneShotEvent event;
    private String sqsMessage;
    private boolean hasTrigger = true;

    private static final Gson gson = new GsonBuilder()
        .setFieldNamingStrategy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    private int index;

    public List<SQSScmConfig> getScmConfigs() {
        return scmConfigs;
    }

    public ProjectFixture setScmConfigs(List<SQSScmConfig> scmConfigs) {
        this.scmConfigs = scmConfigs;
        return this;
    }

    public boolean getShouldStarted() {
        return shouldStarted;
    }

    public ProjectFixture setShouldStarted(boolean shouldStarted) {
        this.shouldStarted = shouldStarted;
        return this;
    }

    public String[] getSendBranches() {
        return sendBranches;
    }

    public ProjectFixture setSendBranches(String... sendBranches) {
        this.sendBranches = sendBranches;
        return this;
    }

    public Long getTimeout() {
        return timeout;
    }

    public ProjectFixture setTimeout(Long timeout) {
        this.timeout = timeout;
        return this;
    }

    public OneShotEvent getEvent() {
        return event;
    }

    public OneShotEvent setEvent(OneShotEvent event) {
        this.event = event;
        return event;
    }

    public String getSqsMessage() {
        return sqsMessage;
    }

    public ProjectFixture setSqsMessage(String sqsMessage) {
        this.sqsMessage = sqsMessage;
        return this;
    }

    public SCM getScm() {
        return scm;
    }

    public ProjectFixture setScm(SCM scm) {
        this.scm = scm;
        return this;
    }

    @Override
    public String toString() {
        return gson.toJson(this);
    }

    @Override
    public MarkIndex setIndex(int index) {
        this.index = index;
        return this;
    }

    @Override
    public int getIndex() {
        return this.index;
    }

    public boolean isSubscribeInternalScm() {
        return subscribeInternalScm;
    }

    public ProjectFixture setSubscribeInternalScm(boolean subscribeInternalScm) {
        this.subscribeInternalScm = subscribeInternalScm;
        return this;
    }

    public String getPipelineScript() {
        return pipelineScript;
    }

    public ProjectFixture setPipelineScript(String pipelineScript) {
        this.pipelineScript = pipelineScript;
        return this;
    }

    public boolean isHasTrigger() {
        return hasTrigger;
    }

    public ProjectFixture setHasTrigger(boolean hasTrigger) {
        this.hasTrigger = hasTrigger;
        return this;
    }
}
