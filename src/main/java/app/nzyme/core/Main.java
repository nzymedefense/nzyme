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

package app.nzyme.core;

import app.nzyme.core.configuration.node.NodeConfiguration;
import app.nzyme.core.logging.Logging;
import com.beust.jcommander.JCommander;
import com.typesafe.config.ConfigException;
import app.nzyme.core.configuration.CLIArguments;
import app.nzyme.core.configuration.IncompleteConfigurationException;
import app.nzyme.core.configuration.InvalidConfigurationException;
import app.nzyme.core.configuration.base.BaseConfiguration;
import app.nzyme.core.configuration.base.BaseConfigurationLoader;
import app.nzyme.core.configuration.node.NodeConfigurationLoader;
import app.nzyme.core.database.DatabaseImpl;
import liquibase.exception.LiquibaseException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    private static final int FAILURE = 1;

    public static void main(String[] argv) {
        Logging.appendCounter();

        final CLIArguments cliArguments = new CLIArguments();

        // Parse CLI arguments.
        JCommander.newBuilder()
                .addObject(cliArguments)
                .build()
                .parse(argv);

        // Override log level if requested.
        if(cliArguments.isDebugMode()) {
            Logging.setRootLoggerLevel(Level.DEBUG);
        }

        if(cliArguments.isTraceMode()) {
            Logging.setRootLoggerLevel(Level.TRACE);
        }

        // Parse configuration.
        BaseConfiguration baseConfiguration = null;
        try {
            baseConfiguration = new BaseConfigurationLoader(new File(cliArguments.getConfigFilePath())).get();
        } catch (InvalidConfigurationException | ConfigException e) {
            LOG.error("Invalid baseconfiguration. Please refer to the example configuration file or documentation.", e);
            System.exit(FAILURE);
        } catch (IncompleteConfigurationException e) {
            LOG.error("Incomplete base configuration. Please refer to the example configuration file or documentation.", e);
            System.exit(FAILURE);
        } catch (FileNotFoundException e) {
            LOG.error("Could not read configuration file.", e);
            System.exit(FAILURE);
        }

        NodeConfiguration nodeConfiguration = null;
        try {
            nodeConfiguration = new NodeConfigurationLoader(new File(cliArguments.getConfigFilePath()), false).get();
        } catch (InvalidConfigurationException | ConfigException e) {
            LOG.error("Invalid configuration. Please refer to the example configuration file or documentation.", e);
            System.exit(FAILURE);
        } catch (IncompleteConfigurationException e) {
            LOG.error("Incomplete configuration. Please refer to the example configuration file or documentation.", e);
            System.exit(FAILURE);
        } catch (FileNotFoundException e) {
            LOG.error("Could not read configuration file.", e);
            System.exit(FAILURE);
        }

        LOG.info("Performance Configuration: {}", nodeConfiguration.performance());

        // Database.
        DatabaseImpl database = new DatabaseImpl(nodeConfiguration);
        try {
            database.initializeAndMigrate();
        } catch (LiquibaseException e) {
            LOG.fatal("Error during database initialization and migration.", e);
            System.exit(FAILURE);
        }

        NzymeNode nzyme = new NzymeNodeImpl(baseConfiguration, nodeConfiguration, database);

        try {
            nzyme.initialize();
        } catch(Exception e) {
            LOG.fatal("Could not initialize nzyme.", e);
            System.exit(FAILURE);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("shutdown-hook");
            nzyme.shutdown();
        }));

        while(true) {
            // https://www.youtube.com/watch?v=Vmb1tqYqyII#t=47s

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { break; /* nein */ }
        }
    }

}
