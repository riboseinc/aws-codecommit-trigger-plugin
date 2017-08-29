/*
 * Copyright 2017 Ribose Inc. <https://www.ribose.com>
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

package com.ribose.jenkins.plugin.awscodecommittrigger.matchers;

import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.Event;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.EventTriggerMatcher;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.matchers.impl.ScmJobEventTriggerMatcher;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;

import java.util.List;

public class EventTriggerMatcherImpl implements EventTriggerMatcher {

    private static final Log log = Log.get(EventTriggerMatcherImpl.class);

    private final EventTriggerMatcher delegate;

    public EventTriggerMatcherImpl() {
        this.delegate = new AndEventTriggerMatcher(
            new ScmJobEventTriggerMatcher()
        );
    }

    @Override
    public boolean matches(List<Event> events, SQSJob job) {
        boolean match = this.delegate.matches(events, job);
        log.debug("Finally, events match status is %s", job, match);
        return match;
    }
}
