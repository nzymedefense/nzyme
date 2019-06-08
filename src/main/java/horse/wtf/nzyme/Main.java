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
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.configuration.ConfigurationLoader;
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

        // Parse configuration.
        Configuration configuration = null;
        try {
            configuration = new ConfigurationLoader(new File(cliArguments.getConfigFilePath()), false).get();
        } catch (ConfigurationLoader.InvalidConfigurationException | ConfigException e) {
            LOG.error("Invalid configuration. Please refer to the example configuration file or documentation.", e);
            System.exit(FAILURE);
        } catch (ConfigurationLoader.IncompleteConfigurationException e) {
            LOG.error("Incomplete configuration. Please refer to the example configuration file or documentation.", e);
            System.exit(FAILURE);
        } catch (FileNotFoundException e) {
            LOG.error("Could not read configuration file.", e);
            System.exit(FAILURE);
        }

        // Override log level if requested.
        if(cliArguments.isDebugMode()) {
            Logging.setRootLoggerLevel(Level.DEBUG);
        }

        if(cliArguments.isTraceMode()) {
            Logging.setRootLoggerLevel(Level.TRACE);
        }

        // Database.
        Database database = new Database(configuration);
        try {
            database.initializeAndMigrate();
        } catch (LiquibaseException e) {
            LOG.fatal("Error during database initialization and migration.", e);
            System.exit(FAILURE);
        }

        Nzyme nzyme = new NzymeImpl(configuration, database);
        nzyme.initialize();

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
