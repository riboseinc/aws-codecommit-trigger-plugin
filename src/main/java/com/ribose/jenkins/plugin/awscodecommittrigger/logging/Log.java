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

package com.ribose.jenkins.plugin.awscodecommittrigger.logging;

import hudson.model.Job;
import org.apache.commons.lang3.ClassUtils;

import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;


public class Log {

    private StreamHandler streamHandler;
    private Logger logger;
    private Class clazz;

    private Log(Class clazz) {
        this.clazz = clazz;
        this.logger = Logger.getLogger(this.clazz.getName());
    }

    private Log(Class clazz, PrintStream logstream) {
        this(clazz);
        this.streamHandler = new StreamHandler(logstream, new SimpleFormatter());
        this.logger.addHandler(streamHandler);
    }

    public static Log get(Class clazz) {
        return new Log(clazz);
    }

    public static Log get(Class clazz, PrintStream logstream) {
        return new Log(clazz, logstream);
    }

    public void error(final String message, final Object... args) {
        write(Level.SEVERE, message, args);
    }

    public void error(String message, final Job job, final Object... args) {
        write(Level.SEVERE, prependJobName(job, message), args);
    }

    public void info(final String message, final Object... args) {
        write(Level.INFO, message, args);
    }

    public void info(String message, final Job job, final Object... args) {
        write(Level.INFO, prependJobName(job, message), args);
    }

    public void debug(final String message, final Object... args) {
        write(Level.CONFIG, message, args);
    }

    public void debug(String message, final Job job, final Object... args) {
        write(Level.CONFIG, prependJobName(job, message), args);
    }

    public void warning(final String message, final Object... args) {
        write(Level.WARNING, message, args);
    }

    private String format(final String message, final Object... args) {
        final String formatted = String.format(message, args);
        final long id = Thread.currentThread().getId();
        return String.format("[%s][thread-%06X] %s", ClassUtils.getAbbreviatedName(this.clazz, 1), id, formatted);
    }

    private void write(final Level level, final String message, final Object... args) {
        String msg = format(message, args);
        if (level == Level.CONFIG) {
            msg = "[DEBUG] " + msg;
        } else if (level == Level.SEVERE) {
            msg = "[ERROR] " + msg;
        }
        this.logger.log(level, msg);
    }

    private String prependJobName(final Job job, String message) {
        return String.format("[job-%s] %s", job.getName(), message);
    }

    public StreamHandler getStreamHandler() {
        return streamHandler;
    }
}
