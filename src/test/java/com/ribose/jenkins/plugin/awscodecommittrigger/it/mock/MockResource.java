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

package com.ribose.jenkins.plugin.awscodecommittrigger.it.mock;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MockResource {

    private static final MockResource instance = new MockResource();

    private final String sqsMessageTemplate;

    private MockResource() {
        try {
            this.sqsMessageTemplate = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("sqscc-msg.json.tpl"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MockResource get() {
        return instance;
    }

    public String getSqsMessageTemplate() {
        return sqsMessageTemplate;
    }

    public String getGitUrl() {
        return "https://git-codecommit.us-west-2.amazonaws.com/v1/repos/testjenkins";
    }
}
