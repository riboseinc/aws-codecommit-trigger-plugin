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

package com.ribose.jenkins.plugin.awscodecommittrigger.model;

import com.amazonaws.services.sqs.model.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.Event;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.MessageParser;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.entities.codecommit.*;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class CodeCommitMessageParser implements MessageParser {

    private static final Log log = Log.get(CodeCommitMessageParser.class);

    private static final String EVENT_SOURCE_CODECOMMIT = "aws:codecommit";

    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @Override
    public List<Event> parseMessage(final Message message) {
        List<Event> events = Collections.emptyList();

        try {
            MessageBody body = gson.fromJson(message.getBody(), MessageBody.class);
            final String json = body.getMessage();
            log.debug("Parse message %s", json);

            if (StringUtils.isEmpty(json)) {
                log.warning("Message contains no text");
                return Collections.emptyList();
            }

            if (!json.startsWith("{") || !json.endsWith("}")) {
                log.warning("Message text is no JSON");
                return Collections.emptyList();
            }

            events = this.parseRecords(json);
        } catch (final com.google.gson.JsonSyntaxException e) {
            log.error("JSON syntax exception, cannot parse message: %s", e);
        }

        return events;
    }

    private List<Event> parseRecords(final String json) {
        Records records = gson.fromJson(json, Records.class);
        List<Event> events = new ArrayList<>(records.size());
        for (final Record record : records) {
            this.parseEvents(events, record);
        }
        return events;
    }

    private void parseEvents(final List<Event> events, final Record record) {
        if (!this.isCodeCommitEvent(record)) {
            return;
        }

        final CodeCommit codeCommit = record.getCodeCommit();

        for (final Reference reference : codeCommit.getReferences()) {
            final Event event = new CodeCommitEvent(record, reference);
            events.add(event);
        }
    }

    private boolean isCodeCommitEvent(final Record record) {
        return StringUtils.equals(EVENT_SOURCE_CODECOMMIT, record.getEventSource());
    }
}
