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

import java.util.Objects;


/**
 * This class consists of {@code static} utility methods usable for argument validation.
 */
public class ThrowIf {

    private static final String IS_NULL   = "The specified argument is null: %s";

    private static final String NOT_EQUAL = "The specified argument does not match the expected value of \"%s\", actual value: \"%s\"";

    /**
     * Throws an {@link IllegalArgumentException} if {@code o} is {@code null}.
     * @param o The object to test for {@code null}.
     * @param name The name of the argument.
     * @throws IllegalArgumentException If {@code o} is {@code null}.
     */
    public static void isNull(final Object o, final String name) {
        if (o == null) {
            final String msg = String.format(IS_NULL, name);
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Throws an {@link IllegalArgumentException} if {@code argument} is not equal to {@code value}.
     * @param argument The argument to test for equality with {@code value}.
     * @param value The value {@code argument} needs to be equal to.
     * @throws IllegalArgumentException If {@code argument} is not equal to {@code value}.
     * @see #notEqual(Object, Object, String, Object...)
     */
    public static void notEqual(final Object argument, final Object value) {
        notEqual(argument, value, NOT_EQUAL, value, argument);
    }

    /**
     * Throws an {@link IllegalArgumentException} if {@code argument} is not equal to {@code value}.
     * @param argument The argument to test for equality with {@code value}.
     * @param value The value {@code argument} needs to be equal to.
     * @param format The detail message for the exception.
     * @param args Optional arguments supplied to the detail message.
     * @throws IllegalArgumentException If {@code argument} is not equal to {@code value}.
     * @see #notEqual(Object, Object)
     */
    public static void notEqual(final Object argument, final Object value, final String format, final Object... args) {
        if (!Objects.equals(argument, value)) {
            final String msg = String.format(format, args);
            throw new IllegalArgumentException(msg);
        }
    }
}
