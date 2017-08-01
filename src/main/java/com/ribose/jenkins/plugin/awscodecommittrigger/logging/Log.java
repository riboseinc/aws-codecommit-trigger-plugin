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

import com.ribose.jenkins.plugin.awscodecommittrigger.SQSTriggerQueue;
import com.ribose.jenkins.plugin.awscodecommittrigger.model.job.SQSJob;
import hudson.model.Job;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ClassUtils;

import java.io.*;
import java.util.logging.*;


public class Log {

    private transient StreamHandler streamHandler;
    private transient Logger logger;
    private transient Class clazz;
    private transient boolean autoFormat = true;//TODO change name?

    private Log(Class clazz) {
        this.clazz = clazz;
        this.logger = Logger.getLogger(this.clazz.getName());
    }

    public static Log get(Class clazz) {
        return new Log(clazz);
    }

    public static Log get(Class clazz, PrintStream out, boolean autoFormat) throws IOException {
        Log log = get(clazz);
        log.autoFormat = autoFormat;

        log.streamHandler = new StreamHandler(out, new SimpleFormatter());
        log.logger.addHandler(log.streamHandler);

        return log;
    }

    public void error(final String message, final Object... args) {
        write(Level.SEVERE, message, args);
    }

    public void error(String message, final Job job, final Object... args) {
        write(Level.SEVERE, prependJobName(job, message), args);
    }

    public void error(String message, final SQSJob job, final Object... args) {
        error(message, job.getJenkinsJob(), args);
    }

    public void info(final String message, final Object... args) {
        write(Level.INFO, message, args);
    }

    public void info(String message, final Job job, final Object... args) {
        info(prependJobName(job, message), args);
    }

    public void info(String message, final SQSJob job, final Object... args) {
        this.info(message, job.getJenkinsJob(), args);
    }

    public void debug(final String message, final Object... args) {
        write(Level.CONFIG, message, args);
    }

    public void debug(String message, final Job job, final Object... args) {
        debug(prependJobName(job, message), args);
    }

    public void debug(String message, final SQSJob job, final Object... args) {
        debug(message, job.getJenkinsJob(), args);
    }

    public void warning(final String message, final Object... args) {
        write(Level.WARNING, message, args);
    }

    private String format(final String message, final Object... args) {
        final String formatted = String.format(message, args);
        final long id = Thread.currentThread().getId();
        return autoFormat ? String.format("[%s][thread-%06X] %s", ClassUtils.getAbbreviatedName(this.clazz, 1), id, formatted) : formatted;
    }

    private void write(final Level level, final String message, final Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof SQSTriggerQueue) {
                args[i] = ((SQSTriggerQueue) args[i]).getUrl();
            }
            else if (args[i] instanceof Throwable) {
                args[i] = ExceptionUtils.getStackTrace((Throwable)args[i]);
            }
        }

        String msg = format(message, args);
        if (level == Level.CONFIG) {
            msg = "[DEBUG] " + msg;
        } else if (level == Level.SEVERE) {
            msg = "[ERROR] " + msg;
        }
        this.logger.logp(level, "[log]", "", msg);
        if (this.streamHandler != null) {
            this.streamHandler.flush();
        }
    }

    private String prependJobName(final Job job, String message) {
        return String.format("[job-%s] %s", job.getName(), message);
    }

    public Logger getLogger() {
        return logger;
    }
}
