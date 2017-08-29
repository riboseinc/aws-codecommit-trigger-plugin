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

package com.ribose.jenkins.plugin.awscodecommittrigger.interfaces;

import com.amazonaws.services.sqs.model.Message;


/**
 * Interface definition for factories that can create {@link MessageParser}s suitable for parsing
 * {@link Message}s returns by an Amazon SQS queue, based on the message type.
 */
public interface MessageParserFactory {

    /**
     * Returns a new parser based on the type of the message that is specified.
     * @param message The {@link Message} for which to create a parser.
     * @return A {@link MessageParser} that can be used to parse the message.
     */
    MessageParser createParser(Message message);

    /**
     * Returns a new parser that can be used to parse messages created by CodeCommit events.
     * @return A {@link MessageParser} suitable for parsing CodeCommit events.
     */
    MessageParser createCodeCommitParser();
}
