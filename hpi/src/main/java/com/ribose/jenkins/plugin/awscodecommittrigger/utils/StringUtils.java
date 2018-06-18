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

package com.ribose.jenkins.plugin.awscodecommittrigger.utils;


import com.amazonaws.services.sqs.model.Message;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class StringUtils {

    public static final Pattern SQS_URL_PATTERN = Pattern
        .compile("^(?:http(?:s)?://)?(?<endpoint>sqs\\..+?\\.amazonaws\\.com)/(?<id>.+?)/(?<name>.*)$");

    public static final Pattern CODE_COMMIT_PATTERN = Pattern.compile("^(?:(https|ssh)?://)?(?<endpoint>git-codecommit\\..+?\\.amazonaws\\.com)/(?<version>.+?)/repos/(?<repoName>.*)$");

    private static final Pattern VERSION_PATTERN = Pattern.compile("\\d*\\^.|\\d+");

    /**
     * Parse csv string and return list of trimmed strings
     *
     * @param str The csv string, can be null
     * @return list of trimmed strings
     */
    public static List<String> parseCsvString(final String str) {
        List<String> result = new ArrayList<>();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(str)) {
            List<String> strs = Arrays.asList(str.split("\\s*,\\s*"));
            for (String s : strs) {
                String item = s.replaceAll("\"", "").replaceAll("'", "").trim();
                if (item.length() > 0) {
                    result.add(item);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Find and return value of uniqueKey in jsonString
     *
     * @param jsonString json string, can not null
     * @param uniqueKey  unique key in jsonString, can not null
     * @return value of <code>jsonString.uniqueKey</code>, or <code>null</code>  if not found
     */
    @CheckForNull
    public static String findByUniqueJsonKey(String jsonString, String uniqueKey) {
        jsonString = jsonString.trim();
        uniqueKey = uniqueKey.trim();

        String regex = String.format("\"%s\"\\s*:\\s*[^\"]*\"([^\"]+)\"", uniqueKey);
        Pattern p = Pattern.compile(regex);
        Matcher matcher = p.matcher(jsonString);
        String value = null;
        if (matcher.find() && matcher.groupCount() > 0) {
            value = matcher.group(1);
        }
        return value;
    }

    private static String findValueByPatter(String string, Pattern pattern, String groupName) {
        String value = null;
        final Matcher matcher = pattern.matcher(string);
        if (matcher.matches()) {
            value = matcher.group(groupName);
        }
        return value;
    }

    /**
     * Parse queueUrl to return the name of that queue
     *
     * @param queueUrl url of the queue, can not null
     * @return the name of queue
     */
    public static String getSqsQueueName(final String queueUrl) {
        return findValueByPatter(queueUrl, SQS_URL_PATTERN, "name");
    }

    /**
     * Parse queueUrl to return the endpoint of that queue
     *
     * @param queueUrl url of the queue, can not null
     * @return the endpoint of that queue
     */
    public static String getSqsEndpoint(final String queueUrl) {
        return findValueByPatter(queueUrl, SQS_URL_PATTERN, "endpoint");
    }

    public static String getCodeCommitRepoName(String codeCommitUrl) {
        return findValueByPatter(codeCommitUrl, CODE_COMMIT_PATTERN, "repoName");
    }

    public static boolean isCodeCommitRepo(String url) {
        if (org.apache.commons.lang3.StringUtils.isBlank(url)) {
            return false;
        }

        String repoName = getCodeCommitRepoName(url);
        if (org.apache.commons.lang3.StringUtils.isEmpty(repoName)) {
            return false;
        }
        return true;
    }

    // return true if "sourceVersion" compatible with "destVersion"
    public static boolean checkCompatibility(@Nullable String sourceVersion, @Nullable String destVersion) {
        if (org.apache.commons.lang3.StringUtils.isBlank(sourceVersion) || org.apache.commons.lang3.StringUtils.isBlank(destVersion)) {
            return false;
        }

        Matcher dest = VERSION_PATTERN.matcher(destVersion);
        Matcher source = VERSION_PATTERN.matcher(sourceVersion);

        Boolean compatible = null;
        while (compatible == null && dest.find()) {
            int destv = Integer.parseInt(dest.group());
            source.find();
            int sourcev = Integer.parseInt(source.group());

            if (destv > sourcev) {
                compatible = false;
            }
            else if (destv <= sourcev) {
                compatible = true;
            }
        }

        return compatible != null && compatible;
    }
}
