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
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import hudson.plugins.git.GitSCM;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;


@RunWith(Parameterized.class)
public class SingleProjectFixtureIT extends AbstractJenkinsIT {

    @Parameterized.Parameter
    public String name;

    @Parameterized.Parameter(1)
    public ProjectFixture fixture;

    private static final GitSCM SCM;
    private static final String SqsMessageTemplate;

    static {
        try {
            SqsMessageTemplate =  IOUtils.toString(StringUtils.getResource(SingleProjectFixtureIT.class, "sqsmsg.json.tpl"), StandardCharsets.UTF_8);
            SCM = MockGitSCM.fromSqsMessage(SqsMessageTemplate);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    @Parameters(name = "{0}")
    public static List<Object[]> fixtures() {
        return Arrays.asList(new Object[][]{
            {
                "should_trigger_branches_without_wildcard_1",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo")
                    .setSubscribedBranches("foo")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_trigger_branches_without_wildcard_2",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo")
                    .setSubscribedBranches("refs/heads/foo")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_trigger_branches_without_wildcard_3",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo/bar")
                    .setSubscribedBranches("refs/heads/foo/bar")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_trigger_branches_without_wildcard_4",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo/bar/foo")
                    .setSubscribedBranches("refs/heads/foo/bar/foo")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_trigger_branches_without_wildcard_5",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo/bar/foo")
                    .setSubscribedBranches("foo/bar/foo")
                    .setShouldStarted(Boolean.TRUE)
            },
            {
                "should_not_trigger_prefix_wildcard_branches_1",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo/bar/foo")
                    .setSubscribedBranches("refs/heads/foo/bar")
                    .setShouldStarted(Boolean.FALSE)
            },
            {
                "should_not_trigger_prefix_wildcard_branches_2",
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

    @Before
    public void beforeRun() throws Exception {
        this.mockAwsSqs.setSqsMessageTemplate(SqsMessageTemplate);
    }

    @Test
    public void shouldPassIt() throws Exception {
        logger.log(Level.INFO, "[RUN] {0}", this.name);
        this.mockAwsSqs.send(this.fixture.getSendBranches());
        this.submitAndAssertFixture(SCM, this.fixture);
        logger.log(Level.INFO, "[DONE] {0}", this.name);
    }
}
