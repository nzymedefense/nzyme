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

import com.beust.jcommander.internal.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.periodicals.PeriodicalManager;
import horse.wtf.nzyme.periodicals.versioncheck.VersioncheckThread;
import horse.wtf.nzyme.probes.dot11.*;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.statistics.StatisticsPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NzymeImpl implements Nzyme {

    private static final Logger LOG = LogManager.getLogger(Nzyme.class);

    public static final int STATS_INTERVAL = 60;

    private final Configuration configuration;
    private final ExecutorService probeExecutor;
    private final Statistics statistics;

    public NzymeImpl(Configuration configuration) {
        this.configuration = configuration;
        this.statistics = new Statistics();

        // TODO TODO TODO XXX: build pool size based on how many probes are configured.
         probeExecutor = Executors.newFixedThreadPool(10, new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("probe-loop-%d")
                .build());
    }

    @Override
    public void initialize() {
        Version version = new Version();
        LOG.info("Initializing probe version: {}.", version.getVersionString());

        // Set up statistics printer.
        final StatisticsPrinter statisticsPrinter = new StatisticsPrinter(statistics, STATS_INTERVAL);
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

        // Periodicals.
        PeriodicalManager periodicalManager = new PeriodicalManager();

        if(configuration.areVersionchecksEnabled()) {
            periodicalManager.scheduleAtFixedRate(new VersioncheckThread(version), 0, 60, TimeUnit.MINUTES);
        } else {
            LOG.info("Versionchecks are disabled.");
        }

        initializeProbes();
    }

    private void initializeProbes() {
        try {
            Dot11Probe monitor = new Dot11MonitorProbe(this, Dot11ProbeConfiguration.create(
                    null,
                    "nzyme-test-1",
                    "wlx00c0ca971216",
                    new ArrayList<Integer>(){{add(8);}},
                    1,
                    "sudo /sbin/iwconfig {interface} channel {channel}"
            ));

            Dot11Probe sender = new Dot11SenderProbe(this, Dot11ProbeConfiguration.create(
                    null,
                    "nzyme-test-1",
                    "wlx00c0ca971216",
                    new ArrayList<Integer>(){{add(8);}},
                    1,
                    "sudo /sbin/iwconfig {interface} channel {channel}"
            ));

            probeExecutor.submit(monitor.loop());
            probeExecutor.submit(sender.loop());
        } catch (Dot11ProbeInitializationException e) {
            e.printStackTrace();
        }

        //////////////////// SECOND
        /*
         * start probes for scenario ROGUE_AP_I
         *   * monitor
         *   * pair
         *     * monitor
         *     * sender
         */
        ////////////////////

        // THIRD: READ PROBE ASSEMBLY FROM NEW CONFIG
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

}
