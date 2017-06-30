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

package io.relution.jenkins.awssqs.util;

import com.amazonaws.AmazonServiceException;

import org.apache.commons.lang3.StringUtils;


/**
 * Provides static methods that can be used to filter exceptions based on their properties.
 */
public class ErrorType {

    /**
     * Returns a value indicating whether the specified service exception has the specified error
     * code and HTTP status.
     * @param e The {@link AmazonServiceException} to test.
     * @param errorCode The error code to match, can be {@code null}.
     * @param httpStatus The HTTP status to match. Use {@code -1} to match any HTTP status.
     * @return {@code true} if the specified exception has the specified error code and HTTP status;
     * otherwise, {@code false}.
     */
    public static boolean is(final AmazonServiceException e, final String errorCode, final int httpStatus) {
        if (e == null) {
            return false;
        }

        if (errorCode != null && !StringUtils.equals(errorCode, e.getErrorCode())) {
            return false;
        }

        if (httpStatus != -1 && e.getStatusCode() != httpStatus) {
            return false;
        }

        return true;
    }
}
