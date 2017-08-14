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

package com.ribose.jenkins.plugin.awscodecommittrigger.matchers.impl;

import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.Event;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.EventTriggerMatcher;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;

import java.util.List;


//TODO deprecated?
@Deprecated
public class SubscribeBranchEventTriggerMatcher implements EventTriggerMatcher {

    private static final Log log = Log.get(SubscribeBranchEventTriggerMatcher.class);

    @Override
    public boolean matches(List<Event> events, SQSJob job) {
//        SQSTrigger trigger = job.getTrigger();
//        List<String> branches = StringUtils.parseCsvString(trigger.getSubscribedBranches());
//        if (branches.size() == 0) {
//            log.debug("Subscribe Branch is empty, using default value `**`", job);
//            branches = Collections.singletonList("**");// default is any branches
//        }
//        //TODO default should be read from SCM config
//        // if no scm defined, we should allow any branch
//
//        log.debug("Events size: %d", job, events.size());
//
//        for (String branch : branches) {
//            BranchSpec branchSpec = new BranchSpec(branch);
//            for (Event event : events) {
//                log.debug("Matching event %s with branch %s", event, branch);
//                if (branchSpec.matches(event.getBranch())) {
//                    log.info("Hurray! Event %s matched branch %s", job, event.getArn(), branch);
//                    return true;
//                }
//            }
//        }
//
//        log.info("No event matched", job);
        return false;
    }
}
