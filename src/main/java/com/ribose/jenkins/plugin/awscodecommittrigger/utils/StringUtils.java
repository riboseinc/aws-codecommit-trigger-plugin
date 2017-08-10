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
import hudson.plugins.git.GitSCM;
import hudson.scm.SCM;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class StringUtils {

    public static final Pattern SQS_URL_PATTERN = Pattern
        .compile("^(?:http(?:s)?://)?(?<endpoint>sqs\\..+?\\.amazonaws\\.com)/(?<id>.+?)/(?<name>.*)$");

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
    public static String findByUniqueJsonKey(String jsonString, String uniqueKey) {
        assert jsonString != null;
        assert uniqueKey != null;

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

    /**
     * Parse queueUrl to return the name of that queue
     *
     * @param queueUrl url of the queue, can not null
     * @return the name of queue
     */
    public static String getSqsQueueName(final String queueUrl) {
        assert queueUrl != null;

        String name = null;
        final Matcher sqsUrlMatcher = SQS_URL_PATTERN.matcher(queueUrl);
        if (sqsUrlMatcher.matches()) {
            name = sqsUrlMatcher.group("name");
        }
        return name;
    }

    /**
     * Parse queueUrl to return the endpoint of that queue
     *
     * @param queueUrl url of the queue, can not null
     * @return the endpoint of that queue
     */
    public static String getSqsEndpoint(final String queueUrl) {
        assert queueUrl != null;

        String name = null;
        final Matcher sqsUrlMatcher = SQS_URL_PATTERN.matcher(queueUrl);
        if (sqsUrlMatcher.matches()) {
            name = sqsUrlMatcher.group("endpoint");
        }
        return name;
    }

    /**
     * Read SCM-URL from given SCM
     *
     * @param scm
     * @return list of SCM-URL
     * */
    public static List<String> getScmUrls(SCM scm) {
        List<String> urls = new ArrayList<>();
        if (scm instanceof GitSCM) {
            final GitSCM git = (GitSCM) scm;
            List<RemoteConfig> repos = git.getRepositories();

            for (RemoteConfig repo : repos) {
                List<URIish> uris = repo.getURIs();
                for (URIish uri : uris) {
                    urls.add(uri.toASCIIString());
                }
            }
        }
        return urls;
    }

    /**
     * Read `MessageId` from given message.body
     *
     * @param message
     * @return Message Id
     * */
    public static String getMessageId(Message message) {
        String body = message.getBody();
        return StringUtils.findByUniqueJsonKey(body, "MessageId");
    }
}
