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
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.model.AbstractProject;
import hudson.plugins.git.GitSCM;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import jenkins.model.Jenkins;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.multiplescms.MultiSCM;

import java.util.List;


public class ScmJobEventTriggerMatcher implements EventTriggerMatcher {

    private static final Log log = Log.get(ScmJobEventTriggerMatcher.class);

    @Override
    public boolean matches(List<Event> events, AbstractProject<?, ?> job) {
        SCM scm = job.getScm();
        if (scm.getClass().isAssignableFrom(NullSCM.class)) {//TODO support NoSCM?? or NoSCM auto-detect?
            log.info("NullSCM detected", job);
            return false;
        }

        for (Event event : events) {
            if (this.matches(event, scm)) {
                log.info("Hurray! Event %s matched SCM ", job, event.getArn(), scm.getKey());
                return true;
            }
        }

        log.info("Found no event matched", job);
        return false;
    }

    private boolean matches(final Event event, final SCM scm) {
        if (event == null || scm == null) {
            return false;
        }

        if (this.isGitScmAvailable() && this.matchesGitSCM(event, scm)) {
            return true;
        } else if (this.isMultiScmAvailable() && this.matchesMultiSCM(event, scm)) {
            return true;
        } else {
            return false;
        }
    }

    private boolean matchesGitSCM(final Event event, final SCM scmProvider) {
        if (!(scmProvider instanceof hudson.plugins.git.GitSCM)) {
            return false;
        }

        final GitSCM git = (GitSCM) scmProvider;
        final List<RemoteConfig> configs = git.getRepositories();

        return this.matchesConfigs(event, configs);
    }

    private boolean matchesMultiSCM(final Event event, final SCM scmProvider) {
        if (!(scmProvider instanceof org.jenkinsci.plugins.multiplescms.MultiSCM)) {
            return false;
        }

        final MultiSCM multiSCM = (MultiSCM) scmProvider;
        final List<SCM> scms = multiSCM.getConfiguredSCMs();

        for (final SCM scm : scms) {
            if (this.matches(event, scm)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesConfigs(final Event event, final List<RemoteConfig> configs) {
        for (final RemoteConfig config : configs) {
            if (this.matchesConfig(event, config)) {
                return true;
            }
        }
        return false;
    }

    private boolean matchesConfig(final Event event, final RemoteConfig config) {
        List<URIish> uris = config.getURIs();
        for (final URIish uri : uris) {
            if (event.isMatch(uri)) {
                log.debug("Event %s matched uri %s", event.getArn(), uri);
                return true;
            }
        }

        log.debug("Found no event matched config: ", event.getArn(), config.getName());
        return false;
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    private boolean isMultiScmAvailable() {
        final Jenkins jenkins = Jenkins.getInstance();

        if (jenkins == null) {
            return false;
        }

        boolean rs = jenkins.getPlugin("multiple-scms") != null;
        log.debug("Multiple-SCMs plugin found: %s", rs);
        return rs;
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE")
    private boolean isGitScmAvailable() {
        final Jenkins jenkins = Jenkins.getInstance();

        if (jenkins == null) {
            return false;
        }

        boolean rs = jenkins.getPlugin("git") != null;
        log.debug("Git plugin found: %s", rs);
        return rs;
    }
}
