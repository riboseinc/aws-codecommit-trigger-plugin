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

import com.ribose.jenkins.plugin.awscodecommittrigger.it.feature.AbstractFreestyleParamsIT;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.fixture.ProjectFixture;
import com.ribose.jenkins.plugin.awscodecommittrigger.it.mock.MockGitSCM;
import hudson.plugins.git.BranchSpec;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class JenkinsIT extends AbstractFreestyleParamsIT {

    /* Subscribe branches integration test, Freestyle Job SCM (type="IR") used as default SCM

    ------------------------------------------------------------------------------------------
    | Trigger on Branch             | Event coming for branch              | Job should RUN? |
    ------------------------------------------------------------------------------------------
    | foo                           | refs/heads/foo                       | True            | 'foo' matched
    --------------------------------|                                      |                 |
    | refs/heads/foo                |                                      |                 | 'refs/heads/foo' matched
    ------------------------------------------------------------------------------------------
    | refs/heads/foo/bar            | refs/heads/foo/bar                   | True            | 'foo/bar' matched
    ------------------------------------------------------------------------------------------
    | refs/heads/foo/bar/foo        | refs/heads/foo/bar/foo               | True            | 'foo/bar/foo' matched
    ------------------------------------------------------------------------------------------
    | refs/heads/foo/bar/foo        | refs/heads/foo/bar                   | False           | 'foo/bar/foo' not matched
    ------------------------------------------------------------------------------------------
    | *foo                          | refs/heads/foo-bar                   | False           | '*foo' not matched
    |                               | refs/heads/bar/foo                   |                 |
    |                               | refs/heads/foo/bar                   |                 |
    ------------------------------------------------------------------------------------------
    | *foo                          | refs/heads/bar/foo                   | True            | '*foo' matched
    |                               | refs/heads/bar-foo                   |                 |
    ------------------------------------------------------------------------------------------
    | foo*                          | refs/heads/foo/bar                   | False           | 'foo*' not matched
    |                               | refs/heads/bar/foo                   |                 |
    |                               | refs/heads/bar-foo                   |                 |
    ------------------------------------------------------------------------------------------
    | foo*                          | refs/heads/bar/foo                   | True            | 'foo*' matched
    |                               | refs/heads/foo-bar                   |                 |
    ------------------------------------------------------------------------------------------
    | *                             | refs/heads/foo/bar                   | False           | '*' not matched
    |                               | refs/heads/bar/foo                   |                 |
    |                               | refs/heads/bar/foo                   |                 |
    ------------------------------------------------------------------------------------------
    | *                             | refs/heads/foo                       | True            | '*' matched
    |                               | refs/heads/foo-bar                   |                 |
    ------------------------------------------------------------------------------------------
    | foo**                         | refs/heads/bar/foo                   | False           | 'foo**' not matched
    |                               | refs/heads/bar/foo                   |                 |
    |                               | refs/heads/bar/foo-bar               |                 |
    |                               | refs/heads/bar/foo/bar               |                 |
    ------------------------------------------------------------------------------------------
    | foo**                         | refs/heads/foo/bar                   | True            | 'foo**' matched
    |                               | refs/heads/foo-bar                   |                 |
    ------------------------------------------------------------------------------------------
    | **                            | refs/heads/foo/bar                   | True            | '**' (all) matched
    |                               | refs/heads/bar/foo                   |                 |
    |                               | refs/heads/bar/foo                   |                 |
    |                               | refs/heads/foo                       |                 |
    |                               | refs/heads/foo-bar                   |                 |
    ------------------------------------------------------------------------------------------
    * */

    @Parameters(name = "{0}")
    public static List<Object[]> fixtures() {
        return Arrays.asList(new Object[][]{
            {
                "'foo' matched",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("foo"))))
                    .setShouldStarted(true)
            },
            {
                "'refs/heads/foo' matched",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/foo"))))
                    .setShouldStarted(true)
            },
            {
                "'foo/bar' matched",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo/bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/foo/bar"))))
                    .setShouldStarted(true)
            },
            {
                "'foo/bar/foo' matched",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo/bar/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/foo/bar/foo"))))
                    .setShouldStarted(true)
            },
            {
                "'foo/bar/foo' not matched",
                new ProjectFixture()//without wildcard
                    .setSendBranches("refs/heads/foo/bar/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("refs/heads/foo/bar"))))
                    .setShouldStarted(false)
            },
            {
                "'*foo' not matched",
                new ProjectFixture()//prefix wildcard
                    .setSendBranches("refs/heads/foo-bar", "refs/heads/bar/foo", "refs/heads/foo/bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("*foo"))))
                    .setShouldStarted(false)
            },
            {
                "'*foo' matched",
                new ProjectFixture()//prefix wildcard
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/bar-foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("*foo"))))
                    .setShouldStarted(true),//triggered because of msg "refs/heads/bar-foo"

            },
            {
                "'foo*' not matched",
                new ProjectFixture()//suffix wildcard
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar-foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("foo*"))))
                    .setShouldStarted(false)
            },
            {
                "'foo*' matched",
                new ProjectFixture()//suffix wildcard
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/foo-bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("foo*"))))
                    .setShouldStarted(true),
            },
            {
                "'*' not matched",
                new ProjectFixture()// "*"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar/foo")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("*"))))
                    .setShouldStarted(false),
            },
            {
                "'*' matched",
                new ProjectFixture()// "*"
                    .setSendBranches("refs/heads/foo", "refs/heads/foo-bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("*"))))
                    .setShouldStarted(true),
            },
            {
                "'foo**' not matched",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/bar/foo", "refs/heads/bar/foo", "refs/heads/bar/foo-bar", "refs/heads/bar/foo/bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("foo**"))))
                    .setShouldStarted(false),
            },
            {
                "'foo**' matched",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/foo-bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("foo**"))))
                    .setShouldStarted(true)
            },
            {
                "'**' (all) matched",
                new ProjectFixture()// "**"
                    .setSendBranches("refs/heads/foo/bar", "refs/heads/bar/foo", "refs/heads/bar/foo", "refs/heads/foo", "refs/heads/foo-bar")
                    .setScm(MockGitSCM.fromUrlAndBranchSpecs(defaultSCMUrl, Collections.singletonList(new BranchSpec("**"))))
                    .setShouldStarted(true),
            }
        });
    }

    @Test
    public void shouldPassIt() throws Exception {
        this.fixture.setSubscribeInternalScm(true);
        super.shouldPassIt();
    }
}
