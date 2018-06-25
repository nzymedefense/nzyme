/*
 *  This file is part of Nzyme.
 *
 *  Nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme;

import com.beust.jcommander.JCommander;
import com.github.joschi.jadconfig.JadConfig;
import com.github.joschi.jadconfig.RepositoryException;
import com.github.joschi.jadconfig.ValidationException;
import com.github.joschi.jadconfig.repositories.PropertiesRepository;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.configuration.CLIArguments;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.deception.bluffs.Bluff;
import horse.wtf.nzyme.deception.bluffs.ProbeRequest;
import horse.wtf.nzyme.periodicals.PeriodicalManager;
import horse.wtf.nzyme.periodicals.versioncheck.VersioncheckThread;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.statistics.StatisticsPrinter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger LOG = LogManager.getLogger(Main.class);

    public static final int STATS_INTERVAL = 60;
    private static final int FAILURE = 1;

    public static void main(String[] argv) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Thread.currentThread().setName("shutdown-hook");
            LOG.info("Shutting down.");
        }));

        Version version = new Version();
        LOG.info("Version: {}.", version.getVersionString());

        final CLIArguments cliArguments = new CLIArguments();
        final Configuration configuration = new Configuration();

        // Parse CLI arguments.
        JCommander.newBuilder()
                .addObject(cliArguments)
                .build()
                .parse(argv);

        // Parse configuration.
        try {
            new JadConfig(new PropertiesRepository(cliArguments.getConfigFilePath()), configuration).process();
        } catch (RepositoryException | ValidationException e) {
            LOG.error("Could not read config.", e);
            Runtime.getRuntime().exit(FAILURE);
        }

        // Override log level if requested.
        if(cliArguments.isDebugMode()) {
            Logging.setRootLoggerLevel(Level.DEBUG);
        }

        if(cliArguments.isTraceMode()) {
            Logging.setRootLoggerLevel(Level.TRACE);
        }

        Bluff pr = new ProbeRequest(configuration);
        try {
            pr.execute();
        } catch (Bluff.BluffExecutionException e) {
            LOG.error("Could not send probe request. Debug information follows.", e);
            pr.debug();
        }

        // Set up statistics printer.
        final Statistics statistics = new Statistics();
        final StatisticsPrinter statisticsPrinter = new StatisticsPrinter(statistics);
        LOG.info("Printing statistics every {} seconds.", STATS_INTERVAL);
        // Statistics printer.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("statistics-%d")
                .build()
        ).scheduleAtFixedRate(() -> {
            LOG.info(statisticsPrinter.print());
            statistics.resetAccumulativeTicks();
        }, STATS_INTERVAL, STATS_INTERVAL, TimeUnit.SECONDS);

        ExecutorService loopExecutor = Executors.newFixedThreadPool(configuration.getChannels().size(), new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("nzyme-loop-%d")
                .build());

        // Periodicals.
        PeriodicalManager periodicalManager = new PeriodicalManager();

        if(configuration.areVersionchecksEnabled()) {
            periodicalManager.scheduleAtFixedRate(new VersioncheckThread(version), 0, 60, TimeUnit.MINUTES);
        } else {
            LOG.info("Versionchecks are disabled.");
        }

        for (Map.Entry<String, ImmutableList<Integer>> config : configuration.getChannels().entrySet()) {
            try {
                Nzyme nzyme = new NzymeImpl(config.getKey(), config.getValue(), cliArguments, configuration, statistics);
                loopExecutor.submit(nzyme.loop());
            } catch (NzymeInitializationException e) {
                LOG.error("Boot error.", e);
                Runtime.getRuntime().exit(FAILURE);
            }
        }

        while(true) {
            // https://www.youtube.com/watch?v=Vmb1tqYqyII#t=47s

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) { break; /* nein */ }
        }
    }

}
