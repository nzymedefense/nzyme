/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.util.HashSet;
import java.util.Set;

public class Logging {

    public static void setRootLoggerLevel(final Level level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.setLevel(level);
        ctx.updateLoggers();
    }

    public static void removeAllAppenders() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        // Collect all appender names
        Set<String> appenderNames = new HashSet<>(loggerConfig.getAppenders().keySet());

        // Remove each appender by name
        for (String appenderName : appenderNames) {
            loggerConfig.removeAppender(appenderName);
        }

        context.updateLoggers();
    }

    public static void appendConsoleLogger() {
        // Get the logger context
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        // Define a pattern layout
        PatternLayout layout = PatternLayout.newBuilder()
                .withPattern("%d{yyyy-MM-dd'T'HH:mm:ss.SSSZ} [%t] %-5level %logger{36} - %msg%n")
                .build();

        // Create the console appender
        ConsoleAppender consoleAppender = ConsoleAppender.newBuilder()
                .setName("ConsoleAppender")
                .setLayout(layout)
                .setTarget(ConsoleAppender.Target.SYSTEM_OUT)
                .build();

        // Start the appender
        consoleAppender.start();

        // Get the root logger config or any specific logger config
        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);

        // Add the appender to the logger config
        loggerConfig.addAppender(consoleAppender, Level.DEBUG, null);

        context.updateLoggers();
    }

    public static void appendCounter() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        CountingAppender countingAppender = CountingAppender.createAppender("CountingAppender", PatternLayout.createDefaultLayout());
        countingAppender.start();

        config.addAppender(countingAppender);

        LoggerConfig loggerConfig = config.getLoggerConfig(LogManager.ROOT_LOGGER_NAME);
        loggerConfig.addAppender(countingAppender, null, null);

        context.updateLoggers();
    }

}
