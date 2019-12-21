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
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.transport.URIish;


public class CodeCommitEvent implements Event {

    private static final Log log = Log.get(CodeCommitEvent.class);

    private final static String HOST = "git-codecommit.%s.amazonaws.com";
    private final static String PATH = "/v1/repos/%s";

    private final String host;
    private final String path;
    private final String branch;
    private final String noPrefixBranch;
    private final String arn;
    private final String user;

    public CodeCommitEvent(final Record record, final Reference reference) {
        this.arn = record.getEventSourceARN();

        final String[] tokens = arn.split(":", 6);
        this.host = String.format(HOST, tokens[3]);
        this.path = String.format(PATH, tokens[5]);

        this.branch = reference.getReference();
        this.noPrefixBranch = reference.getReference().replaceAll("(refs/heads|refs/remotes|remotes)", ""); //truncate all possible git remote prefix, ref hudson.plugins.git.BranchSpec.getPattern
        this.user = record.getUserIdentityARN();
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
        return this.user;
    }

    @Override
    public String getBranch() {
        return this.branch;
    }

    @Override
    public String getNoPrefixBranch() {
        return this.noPrefixBranch;
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

        // from https://github.com/riboseinc/aws-codecommit-trigger-plugin/issues/54#issuecomment-546503407
        // ignore the difference of the last slash
        String p1 =  this.path.endsWith("/") ? this.path : this.path + "/";
        String p2 = uri.getPath().endsWith("/") ? uri.getPath() : uri.getPath() + "/";

        if (!StringUtils.equalsIgnoreCase(p1, p2)) {
            log.debug("Event %s not match path %s", this.getArn(), uri.getPath());
            return false;
        }

        log.debug("Event %s match uri %s", this.getArn(), uri);
        return true;
    }

    public String getArn() {
        return arn;
    }
}
