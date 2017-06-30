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

package io.relution.jenkins.awssqs.logging;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Log {

    private static final Logger LOGGER;

    static {
        LOGGER = Logger.getLogger(Log.class.getName());
    }

    private static String format(final String message, final Object... args) {
        final String formatted = String.format(message, args);
        final long id = Thread.currentThread().getId();
        return String.format("%06X %s", id, formatted);
    }

    private static void write(final Level level, final String message, final Object... args) {
        final String msg = format(message, args);
        LOGGER.log(level, msg);
    }

    private static void write(final Level level, final Throwable thrown, final String message, final Object... args) {
        final String msg = format(message, args);
        LOGGER.log(level, msg, thrown);
    }

    public static void finest(final String message, final Object... args) {
        write(Level.FINEST, message, args);
    }

    public static void finer(final String message, final Object... args) {
        write(Level.FINER, message, args);
    }

    public static void fine(final String message, final Object... args) {
        write(Level.FINE, message, args);
    }

    public static void info(final String message, final Object... args) {
        write(Level.INFO, message, args);
    }

    public static void warning(final String message, final Object... args) {
        write(Level.WARNING, message, args);
    }

    public static void severe(final String message, final Object... args) {
        write(Level.SEVERE, message, args);
    }

    public static void severe(final Throwable thrown, final String message, final Object... args) {
        write(Level.SEVERE, thrown, message, args);
    }
}
