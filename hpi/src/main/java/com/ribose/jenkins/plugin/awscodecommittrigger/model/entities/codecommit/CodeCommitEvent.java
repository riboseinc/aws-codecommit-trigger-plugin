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

package com.ribose.jenkins.plugin.awscodecommittrigger.model.entities.codecommit;

import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.Event;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.transport.URIish;


public class CodeCommitEvent implements Event {

    private static final Log log = Log.get(CodeCommitEvent.class);

    private final static String HOST = "git-codecommit.%s.amazonaws.com";
    private final static String PATH = "/v1/repos/%s";

    private final String host;
    private final String path;
    private final String branch;
    private final String arn;

    public CodeCommitEvent(final Record record, final Reference reference) {
        this.arn = record.getEventSourceARN();

        final String[] tokens = arn.split(":", 6);
        this.host = String.format(HOST, tokens[3]);
        this.path = String.format(PATH, tokens[5]);

        this.branch = reference.getName();
    }

    @Override
    public String getHost() {
        return this.host;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getUser() {
        return null;
    }

    @Override
    public String getBranch() {
        return this.branch;
    }

    @Override
    public boolean isMatch(final URIish uri) {
        if (uri == null) {
            return false;
        }

        if (!StringUtils.equals(this.host, uri.getHost())) {
            log.debug("Event %s not match host %s", this.getArn(), uri.getHost());
            return false;
        }

        if (!StringUtils.equals(this.path, uri.getPath())) {
            log.debug("Event %s not match path %s", this.getArn(), uri.getPath());
            return false;
        }

        log.debug("Event %s match uri %s", this.getArn(), uri);
        return true;
    }

    public String getArn() {
        return arn;
    }

    @Override
    public String toString() {
        return "CodeCommitEvent{" +
            "host='" + host + '\'' +
            ", path='" + path + '\'' +
            ", branch='" + branch + '\'' +
            ", arn='" + arn + '\'' +
            '}';
    }
}
