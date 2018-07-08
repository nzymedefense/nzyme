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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.dot11.Dot11FrameInterceptor;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.periodicals.PeriodicalManager;
import horse.wtf.nzyme.periodicals.versioncheck.VersioncheckThread;
import horse.wtf.nzyme.probes.dot11.*;
import horse.wtf.nzyme.rest.CORSFilter;
import horse.wtf.nzyme.rest.InjectionBinder;
import horse.wtf.nzyme.rest.resources.PingResource;
import horse.wtf.nzyme.rest.resources.system.MetricsResource;
import horse.wtf.nzyme.rest.resources.system.StatisticsResource;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.statistics.StatisticsPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
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
    private final MetricRegistry metrics;

    public NzymeImpl(Configuration configuration) {
        this.configuration = configuration;
        this.statistics = new Statistics();
        this.metrics = new MetricRegistry();

         probeExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
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

        // Metrics JMX reporter.
        final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();

        // Periodicals.
        PeriodicalManager periodicalManager = new PeriodicalManager();

        if(configuration.areVersionchecksEnabled()) {
            periodicalManager.scheduleAtFixedRate(new VersioncheckThread(version), 0, 60, TimeUnit.MINUTES);
        } else {
            LOG.info("Versionchecks are disabled.");
        }

        // Spin up REST API.
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new CORSFilter());
        resourceConfig.register(new InjectionBinder(this));

        // Register resources.
        resourceConfig.register(PingResource.class);
        resourceConfig.register(MetricsResource.class);
        resourceConfig.register(StatisticsResource.class);

        final HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://127.0.0.1:3001"), resourceConfig); // TODO make configurable
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdownNow)); // Properly stop server on shutdown.

        // Start server.
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start REST API.", e);
        }

        initializeProbes();
    }

    private void initializeProbes() {
        try {
            Dot11Probe monitor = new Dot11MonitorProbe(this, Dot11ProbeConfiguration.create(
                    "monitor-main",
                    null,
                    "nzyme-test-1",
                    "wlx00c0ca971216",
                    new ArrayList<Integer>(){{add(1);add(2);add(3);add(4);add(5);add(6);add(7);add(8);add(9);add(10);add(11);}},
                    1,
                    "sudo /sbin/iwconfig {interface} channel {channel}"
            ), getMetrics());

            Dot11MonitorProbe.configureAsBroadMonitor((Dot11MonitorProbe) monitor);

            monitor.addFrameInterceptor(new Dot11FrameInterceptor<Dot11BeaconFrame>() {
                @Override
                public void intercept(Dot11BeaconFrame frame) {
                    if ("FOOKED".equals(frame.ssid())) {
                        LOG.info("CONTACT for bandit {} at signal strength {}dBm. Trapped by [ROGUE_AP_II].", frame.transmitter(), frame.metaInformation().getAntennaSignal());
                    }
                    // CONTACT at -71db for fsdfsdfsd
                }

                @Override
                public byte forSubtype() {
                    return Dot11FrameSubtype.BEACON;
                }
            });

            Dot11Probe sender = new Dot11SenderProbe(this, Dot11ProbeConfiguration.create(
                    "sender-1",
                    null,
                    "nzyme-test-1",
                    "wlx00c0ca971201",
                    new ArrayList<Integer>(){{add(8);}},
                    1,
                    "sudo /sbin/iwconfig {interface} channel {channel}"
            ), getMetrics());

            /*sender.scheduleActions(1, TimeUnit.SECONDS, new Dot11SenderAction() {
                // send bluff
            });*/

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

    @Override
    public MetricRegistry getMetrics() {
        return metrics;
    }

}
