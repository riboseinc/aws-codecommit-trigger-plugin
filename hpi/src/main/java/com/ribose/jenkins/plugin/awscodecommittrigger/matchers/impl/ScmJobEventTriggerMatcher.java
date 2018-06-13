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

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSScmConfig;
import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTrigger;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.Event;
import com.ribose.jenkins.plugin.awscodecommittrigger.interfaces.EventTriggerMatcher;
import com.ribose.jenkins.plugin.awscodecommittrigger.logging.Log;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;
import hudson.plugins.git.BranchSpec;
import hudson.plugins.git.GitSCM;
import hudson.scm.NullSCM;
import hudson.scm.SCM;
import jenkins.model.Jenkins;
import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;
import org.jenkinsci.plugins.multiplescms.MultiSCM;

import java.util.ArrayList;
import java.util.List;


public class ScmJobEventTriggerMatcher implements EventTriggerMatcher {

    private static final Log log = Log.get(ScmJobEventTriggerMatcher.class);

    @Override
    public boolean matches(List<Event> events, SQSJob job) {//TODO load scm list
        SQSTrigger trigger = job.getTrigger();
        List<SQSScmConfig> scmConfigs = new ArrayList<>();

        List<SQSScmConfig> triggerScms = trigger.getSqsScmConfigs();
        if (CollectionUtils.isNotEmpty(triggerScms)) {
            scmConfigs.addAll(triggerScms);
        }
        if (trigger.isSubscribeInternalScm()) {
            scmConfigs.add(new SQSScmConfig(SQSScmConfig.Type.IR, null, null));
        }

        List<SCM> scms = new ArrayList<>();
        for (SQSScmConfig scmConfig : scmConfigs) {
            switch (scmConfig.getType()) {
                case IR:
                    scms.addAll(job.getScmList());
                    break;

                case ER:
                    scms.add(scmConfig.toGitSCM());
                    break;
            }
        }

        log.debug("Events size: %d, SCMs size: %d", job, events.size(), scms.size());

        for (SCM scm : scms) {
            if (scm.getClass().isAssignableFrom(NullSCM.class)) {
                log.debug("NullSCM detected, continue match next SCM", job);
                continue;
            }

            for (Event event : events) {
                log.debug("Matching event %s with SCM %s", event, scm.getKey());
                if (this.matches(event, scm)) {
                    log.debug("Hurray! Event %s matched SCM %s", job, event.getArn(), scm.getKey());
                    return true;
                }
            }
        }

        log.debug("No event matched", job);
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
        if (!(scmProvider instanceof GitSCM)) {
            return false;
        }

        final GitSCM git = (GitSCM) scmProvider;
        final List<RemoteConfig> configs = git.getRepositories();

        boolean matched = this.matchesConfigs(event, configs);
        matched = matched && this.matchBranch(event, git.getBranches());
        return matched;
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

    private boolean matchBranch(final Event event, final List<BranchSpec> branchSpecs) {//TODO use it
        for (BranchSpec branchSpec : branchSpecs) {
            if (branchSpec.matches(event.getBranch())) {
                log.debug("Event branch: %s matched branch: %s", event.getBranch(), branchSpec.getName());
                return true;
            }
            else if (branchSpec.matches(event.getNoPrefixBranch())) {
                log.debug("Event no-prefix-branch: %s matched branch: %s", event.getNoPrefixBranch(), branchSpec.getName());
                return true;
            }
        }

        log.debug("Found no event matched any branch", event.getArn());
        return false;
    }

    private boolean matchesConfig(final Event event, final RemoteConfig config) {
        return getMatchesConfig(event, config) != null;
    }

    private URIish getMatchesConfig(final Event event, final RemoteConfig config) {
        List<URIish> uris = config.getURIs();
        for (final URIish uri : uris) {
            if (event.isMatch(uri)) {//TODO use here matchBranch(event, branchSpec)
                log.debug("Event %s matched uri %s", event.getArn(), uri);
                return uri;
            }
        }

        log.debug("Found no event matched config: ", event.getArn(), config.getName());
        return null;
    }

    private boolean isMultiScmAvailable() {
        final Jenkins jenkins = Jenkins.getActiveInstance();
        boolean hasPlugin = jenkins.getPlugin("multiple-scms") != null;
        log.debug("Multiple-SCMs plugin found: %s", hasPlugin);
        return hasPlugin;
    }

    private boolean isGitScmAvailable() {
        final Jenkins jenkins = Jenkins.getActiveInstance();
        boolean hasPlugin = jenkins.getPlugin("git") != null;
        log.debug("Git plugin found: %s", hasPlugin);
        return hasPlugin;
    }
}
