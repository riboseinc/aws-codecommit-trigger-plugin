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

package com.ribose.jenkins.plugin.awscodecommittrigger.matchers;

import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.Event;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.EventTriggerMatcher;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;
import org.apache.commons.lang3.ClassUtils;

import java.util.List;


public class AndEventTriggerMatcher extends AbstractEventTriggerMatcher {

    private static final Log log = Log.get(AndEventTriggerMatcher.class);

    public AndEventTriggerMatcher(EventTriggerMatcher... matchers) {
        super(matchers);
    }

    @Override
    public boolean matches(List<Event> events, SQSJob job) {
        for (EventTriggerMatcher matcher : matchers) {
            log.debug("Test if any event not match using %s", ClassUtils.getAbbreviatedName(matcher.getClass(), 1));
            if (!matcher.matches(events, job)) {
                return false;
            }
        }

        log.debug("OK! At least one event matched");
        return true;
    }
}
