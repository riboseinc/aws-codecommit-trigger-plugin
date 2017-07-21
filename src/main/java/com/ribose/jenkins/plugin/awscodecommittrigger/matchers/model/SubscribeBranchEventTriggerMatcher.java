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

package com.ribose.jenkins.plugin.awscodecommittrigger.matchers.model;

import hudson.model.AbstractProject;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.Event;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.EventTriggerMatcher;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;

import java.util.Arrays;
import java.util.List;

public class SubscribeBranchEventTriggerMatcher implements EventTriggerMatcher {

    @Override
    public boolean matches(final List<Event> events, final AbstractProject<?, ?> job) {
        if (events == null || job == null) {
            return false;
        }

        SQSTrigger trigger = job.getTrigger(SQSTrigger.class);
        if (trigger == null) {
            Log.info("Job '%s': Trigger is not an instance of class SQSTrigger.", job.getName());
            return false;
        }

        List<String> branches = StringUtils.parseCsvString(trigger.getSubscribedBranches());
        if (branches.size() == 0) {
            branches = Arrays.asList("**");// default is any branches
        }

        for (String branch : branches) {
            final String pattern = StringUtils.parseWildcard(branch);
            for (Event event : events) {
                if (event.getBranch().matches(pattern)) {
                    Log.info("[%s] Job '%s': event matches by branch '%s'", SubscribeBranchEventTriggerMatcher.class.getSimpleName(), job.getName(), branch);
                    return true;
                }
            }
        }

        Log.info("[%s] Event(s) did not match job '%s'", SubscribeBranchEventTriggerMatcher.class.getSimpleName(),  job.getName());
        return false;
    }
}
