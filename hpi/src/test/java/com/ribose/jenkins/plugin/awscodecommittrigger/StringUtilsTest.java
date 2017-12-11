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

package com.ribose.jenkins.plugin.awscodecommittrigger;

import com.ribose.jenkins.plugin.awscodecommittrigger.utils.StringUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ClassUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtilsTest {

    @Test
    public void testBranchMatch() {
        String regex = "(.*)feature/sqstest";
        String amzCodecCommitRef = "refs/heads/feature/sqstest";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(amzCodecCommitRef);
        String found = null;
        while (matcher.find()) {
            found = matcher.group();
        }
        Assertions.assertThat(found).matches(regex);
    }

    @Test
    public void testParseCsvString() {
        String str = "test1, test2, test3";
        List<String> items = StringUtils.parseCsvString(str);
        Assertions.assertThat(items).containsExactly("test1", "test2", "test3");

        str = "test1, test2, test3,,,";
        items = StringUtils.parseCsvString(str);
        Assertions.assertThat(items).containsExactly("test1", "test2", "test3");

        str = "test1, test2, test3, , , test4, \"  test5 test6\"";
        items = StringUtils.parseCsvString(str);
        Assertions.assertThat(items).containsExactly("test1", "test2", "test3", "test4", "test5 test6");
    }

    @Test
    public void testFindByUniqueJsonKey() throws IOException {
        String sqsResponse = IOUtils.toString(Utils.getResource(StringUtilsTest.class, "sqsmsg.json"), StandardCharsets.UTF_8);
        String messageId = StringUtils.findByUniqueJsonKey(sqsResponse, "MessageId");
        String timestamp = StringUtils.findByUniqueJsonKey(sqsResponse, "Timestamp");
        String topicArn = StringUtils.findByUniqueJsonKey(sqsResponse, "TopicArn");
        String signature = StringUtils.findByUniqueJsonKey(sqsResponse, "Signature");
        Assertions.assertThat(sqsResponse).contains(messageId, timestamp, topicArn, signature);

        sqsResponse = "not a json string";
        messageId = StringUtils.findByUniqueJsonKey(sqsResponse, "MessageId");
        Assertions.assertThat(messageId).isNull();
    }

    @Test
    public void testGetRepoName() {
        Pattern pattern = Pattern.compile("(test)");
        Matcher matcher = pattern.matcher("/v1/repos/test");
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    @Test
    public void testShortClassName() {
        Assertions.assertThat(ClassUtils.getAbbreviatedName(StringUtilsTest.class, 1).length()).isLessThan(StringUtilsTest.class.getName().length());
    }

    @Test
    public void testParseCodeCommitUrl() {
        Assertions.assertThat(StringUtils.getCodeCommitRepoName("https://git-codecommit.us-west-2.amazonaws.com/v1/repos/testjenkins")).isEqualToIgnoringCase("testjenkins");
        Assertions.assertThat(StringUtils.getCodeCommitRepoName("ssh://git-codecommit.us-west-2.amazonaws.com/v1/repos/testjenkins")).isEqualToIgnoringCase("testjenkins");
        Assertions.assertThat(StringUtils.getCodeCommitRepoName("ssh://git-codecommit.us-west-2.amazonaws.com/v1/repos/testjenkins2")).isNotEqualToIgnoringCase("testjenkins");
        Assertions.assertThat(StringUtils.getCodeCommitRepoName("http://git-codecommit.us-west-2.amazonaws.com/v1/repos/testjenkins")).isNullOrEmpty();
        Assertions.assertThat(StringUtils.getCodeCommitRepoName("http://git-codecommit.us-west-2.amazonaws.com/v1/repos/testjenkins")).isNotEqualToIgnoringCase("testjenkins");
    }

    @Test
    public void testCompatible() {
        Assertions.assertThat(StringUtils.checkCompatibility("1.16", "1.7")).isTrue();
        Assertions.assertThat(StringUtils.checkCompatibility("1.16", "2.0")).isFalse();
        Assertions.assertThat(StringUtils.checkCompatibility("1.16", "1.15")).isTrue();
        Assertions.assertThat(StringUtils.checkCompatibility("2.0-SNAPSHOT", "2.0")).isTrue();
    }
}
