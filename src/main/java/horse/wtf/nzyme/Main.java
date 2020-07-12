/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme;

import com.beust.jcommander.JCommander;
import com.typesafe.config.ConfigException;
import horse.wtf.nzyme.configuration.CLIArguments;
import horse.wtf.nzyme.configuration.IncompleteConfigurationException;
import horse.wtf.nzyme.configuration.InvalidConfigurationException;
import horse.wtf.nzyme.configuration.base.BaseConfiguration;
import horse.wtf.nzyme.configuration.base.BaseConfigurationLoader;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.configuration.leader.LeaderConfigurationLoader;
import horse.wtf.nzyme.configuration.tracker.TrackerConfiguration;
import horse.wtf.nzyme.configuration.tracker.TrackerConfigurationLoader;
import horse.wtf.nzyme.database.Database;
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


        switch (baseConfiguration.mode()) {
            case LEADER:
                LeaderConfiguration leaderConfiguration = null;
                try {
                    leaderConfiguration = new LeaderConfigurationLoader(new File(cliArguments.getConfigFilePath()), false).get();
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


                // Database.
                Database database = new Database(leaderConfiguration);
                try {
                    database.initializeAndMigrate();
                } catch (LiquibaseException e) {
                    LOG.fatal("Error during database initialization and migration.", e);
                    System.exit(FAILURE);
                }

                NzymeLeader nzyme = new NzymeLeaderImpl(baseConfiguration, leaderConfiguration, database);
                nzyme.initialize();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    Thread.currentThread().setName("shutdown-hook");
                    nzyme.shutdown();
                }));
                break;
            case TRACKER:
                TrackerConfiguration trackerConfiguration = null;
                try {
                    trackerConfiguration = new TrackerConfigurationLoader(new File(cliArguments.getConfigFilePath())).get();
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

                NzymeTracker tracker = new NzymeTrackerImpl(baseConfiguration, trackerConfiguration);
                tracker.initialize();
        }

        while(true) {
            // https://www.youtube.com/watch?v=Vmb1tqYqyII#t=47s

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { break; /* nein */ }
        }
    }

}
