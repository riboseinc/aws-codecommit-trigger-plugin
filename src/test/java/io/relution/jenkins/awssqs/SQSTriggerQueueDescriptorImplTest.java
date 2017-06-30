/*
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

package io.relution.jenkins.awssqs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import hudson.util.FormValidation;
import io.relution.jenkins.awssqs.SQSTriggerQueue.DescriptorImpl;


public class SQSTriggerQueueDescriptorImplTest {

    @Test
    public void shouldAcceptNameAsNameOrUrl() {
        final DescriptorImpl descriptor = new DescriptorImpl();
        final FormValidation validation = descriptor.doCheckNameOrUrl("test-queue");

        assertThat(validation.kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    public void shouldAcceptSqsUrlAsNameOrUrl() {
        final DescriptorImpl descriptor = new DescriptorImpl();
        final FormValidation validation = descriptor.doCheckNameOrUrl("https://sqs.us-east-1.amazonaws.com/929548749884/relution-queue-mytest7");

        assertThat(validation.kind).isEqualTo(FormValidation.Kind.OK);
    }

    @Test
    public void shouldNotAcceptEmptyUrlAsNameOrUrl() {
        final DescriptorImpl descriptor = new DescriptorImpl();
        final FormValidation validation = descriptor.doCheckNameOrUrl(null);

        assertThat(validation.kind).isEqualTo(FormValidation.Kind.WARNING);
    }

    @Test
    public void shouldNotAcceptCodeCommitUrlAsNameOrUrl() {
        final DescriptorImpl descriptor = new DescriptorImpl();
        final FormValidation validation = descriptor.doCheckNameOrUrl("https://git-codecommit.us-east-1.amazonaws.com/v1/repos/relution-mytest7");

        assertThat(validation.kind).isEqualTo(FormValidation.Kind.ERROR);
    }

    @Test
    public void shouldNotAcceptAnyUrlAsNameOrUrl() {
        final DescriptorImpl descriptor = new DescriptorImpl();
        final FormValidation validation = descriptor.doCheckNameOrUrl("http://www.google.de");

        assertThat(validation.kind).isEqualTo(FormValidation.Kind.ERROR);
    }
}
