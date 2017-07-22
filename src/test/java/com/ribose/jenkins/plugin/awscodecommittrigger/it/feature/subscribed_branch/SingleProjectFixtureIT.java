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

package com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.subscribed_branch;

import com.ribose.jenkins.plugin.awscodecommittrigger.it.AbstractJenkinsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import hudson.util.OneShotEvent;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;


@RunWith(Parameterized.class)
public class SingleProjectFixtureIT extends AbstractJenkinsIT {

    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public ProjectFixture projectFixture;

    @Parameters(name = "{0}")
    public static List<Object[]> fixtures() {
        return Arrays.asList(new Object[][]{
            {
                "should_trigger_branches_without_wildcard",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo")
                    .setSubscribedBranches("foo")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_trigger_branches_without_wildcard",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo")
                    .setSubscribedBranches("refs/heads/foo")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_not_trigger_prefix_wildcard_branches",
                new ProjectFixture()//prefix wildcard
                    .setSendBranches("refs/heads/foo-bar", "refs/heads/bar/foo", "refs/heads/foo/bar")
                    .setSubscribedBranches("*foo")
                    .setShouldStarted(Boolean.FALSE)
            },
            {
                "should_trigger_prefix_wildcard_branches",
                new ProjectFixture()//prefix wildcard
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/bar-foo")
                    .setSubscribedBranches("*foo")
                    .setShouldStarted(Boolean.TRUE),//triggered because of msg "refs/heads/bar-foo"

            },
            {
                "should_not_trigger_suffix_wildcard_branches",
                new ProjectFixture()//suffix wildcard
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar-foo")
                    .setSubscribedBranches("foo*")
                    .setShouldStarted(Boolean.FALSE)
            },
            {
                "should_trigger_suffix_wildcard_branches",
                new ProjectFixture()//suffix wildcard
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/foo-bar")
                    .setSubscribedBranches("foo*")
                    .setShouldStarted(Boolean.TRUE),//triggered because of msg "refs/heads/foo-bar"
            },
            {
                "should_not_trigger_single_star_branches",
                new ProjectFixture()// "*"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar/foo")
                    .setSubscribedBranches("*")
                    .setShouldStarted(Boolean.FALSE),
            },
            {
                "should_trigger_single_star_branches",
                new ProjectFixture()// "*"
                    .setSendBranches("refs/heads/foo", "refs/heads/foo-bar")
                    .setSubscribedBranches("*")
                    .setShouldStarted(Boolean.TRUE),
            },
            {
                "should_not_trigger_double_stars_branches",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/bar/foo", "refs/heads/bar/foo-bar", "refs/heads/bar/foo/bar")
                    .setSubscribedBranches("foo**")
                    .setShouldStarted(Boolean.FALSE),
            },
            {
                "should_trigger_double_stars_branches",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/foo-bar")
                    .setSubscribedBranches("foo**")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_trigger_all_branches",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar/foo", "refs/heads/foo", "refs/heads/foo-bar")
                    .setSubscribedBranches("**")
                    .setShouldStarted(Boolean.TRUE),
            }
        });
    }

    @Test
    public void shouldPassProjectFixture() throws Exception {
        logger.log(Level.INFO, "[RUN] {0}", this.name);
        logger.log(Level.FINEST, "[FIXTURE] {0}", this.projectFixture);
        this.mockAwsSqs.send(this.projectFixture.getSendBranches());
        OneShotEvent buildStarted = submitGitScmProject(this.getScm(), this.projectFixture.getSubscribedBranches());
        buildStarted.block(this.projectFixture.getTimeout());
        Assertions.assertThat(buildStarted.isSignaled()).isEqualTo(this.projectFixture.getShouldStarted());
        logger.log(Level.INFO, "[DONE] {0}", this.name);
    }
}
