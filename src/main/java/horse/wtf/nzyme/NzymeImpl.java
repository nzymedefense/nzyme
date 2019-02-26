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
import com.codahale.metrics.jvm.*;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.alerts.AlertsService;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.configuration.Dot11MonitorDefinition;
import horse.wtf.nzyme.dot11.interceptors.StatisticsInterceptorSet;
import horse.wtf.nzyme.dot11.interceptors.UnexpectedSSIDInterceptorSet;
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeInitializationException;
import horse.wtf.nzyme.dot11.interceptors.UnexpectedBSSIDInterceptorSet;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.periodicals.PeriodicalManager;
import horse.wtf.nzyme.periodicals.versioncheck.VersioncheckThread;
import horse.wtf.nzyme.rest.CORSFilter;
import horse.wtf.nzyme.rest.InjectionBinder;
import horse.wtf.nzyme.rest.ObjectMapperProvider;
import horse.wtf.nzyme.rest.resources.AlertsResource;
import horse.wtf.nzyme.rest.resources.NetworksResource;
import horse.wtf.nzyme.rest.resources.PingResource;
import horse.wtf.nzyme.rest.resources.system.MetricsResource;
import horse.wtf.nzyme.rest.resources.system.ProbesResource;
import horse.wtf.nzyme.rest.resources.system.StatisticsResource;
import horse.wtf.nzyme.rest.resources.system.SystemResource;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.statistics.StatisticsPrinter;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NzymeImpl implements Nzyme {

    private static final Logger LOG = LogManager.getLogger(Nzyme.class);

    public static final int STATS_INTERVAL = 60;

    private final Configuration configuration;
    private final ExecutorService probeExecutor;
    private final Statistics statistics;
    private final MetricRegistry metrics;
    private final Networks networks;
    private final SystemStatus systemStatus;
    private final OUIManager ouiManager;

    private final List<Dot11Probe> probes;
    private final AlertsService alerts;

    private HttpServer httpServer;

    public NzymeImpl(Configuration configuration) {
        this.configuration = configuration;
        this.statistics = new Statistics();
        this.metrics = new MetricRegistry();
        this.probes = Lists.newArrayList();
        this.networks = new Networks(this);
        this.systemStatus = new SystemStatus();

        // Register JVM metrics.
        this.metrics.register("gc", new GarbageCollectorMetricSet());
        this.metrics.register("classes", new ClassLoadingGaugeSet());
        this.metrics.register("fds", new FileDescriptorRatioGauge());
        this.metrics.register("jvm", new JvmAttributeGaugeSet());
        this.metrics.register("mem", new MemoryUsageGaugeSet());
        this.metrics.register("threadstates", new ThreadStatesGaugeSet());

        // Set up initial system status.
        this.systemStatus.setStatus(SystemStatus.TYPE.RUNNING);
        this.systemStatus.setStatus(SystemStatus.TYPE.TRAINING);

        this.ouiManager = new OUIManager(this.metrics);

        this.alerts = new AlertsService(this);

        // Disable TRAINING status when training period is over.
        LOG.info("Training period ends in <{}> seconds.", configuration.getAlertingTrainingPeriodSeconds());
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            LOG.info("Training period is over!");
            systemStatus.unsetStatus(SystemStatus.TYPE.TRAINING);
        }, configuration.getAlertingTrainingPeriodSeconds(), TimeUnit.SECONDS);

        probeExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("probe-loop-%d")
                .build());
    }

    @Override
    public void initialize() {
        Version version = new Version();
        LOG.info("Initializing probe version: {}.", version.getVersionString());

        // Start OUI manager and regularly refresh it.
        try {
            this.ouiManager.fetchAndUpdate();

            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("oui-manager-%d")
                    .build())
                    .scheduleAtFixedRate(() -> {
                        try {
                            ouiManager.fetchAndUpdate();
                        } catch (IOException e) {
                            LOG.error("Could not fetch and update OUI list.", e);
                        }
                    }, 12, 12, TimeUnit.HOURS);
        } catch (IOException e) {
            LOG.error("Could not initialize OUIs.", e);
        }

        // Set up networks printer.
        final StatisticsPrinter statisticsPrinter = new StatisticsPrinter(statistics, STATS_INTERVAL);
        LOG.info("Printing networks every {} seconds.", STATS_INTERVAL);

        // Statistics printer.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("networks-%d")
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

        // Spin up REST API and web interface.
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new CORSFilter());
        resourceConfig.register(new InjectionBinder(this));
        resourceConfig.register(new ObjectMapperProvider());

        // Register resources.
        resourceConfig.register(PingResource.class);
        resourceConfig.register(AlertsResource.class);
        resourceConfig.register(ProbesResource.class);
        resourceConfig.register(MetricsResource.class);
        resourceConfig.register(StatisticsResource.class);
        resourceConfig.register(NetworksResource.class);
        resourceConfig.register(SystemResource.class);

        java.util.logging.Logger.getLogger("org.glassfish.grizzly").setLevel(Level.SEVERE);
        httpServer = GrizzlyHttpServerFactory.createHttpServer(configuration.getRestListenUri(), resourceConfig);
        LOG.info("Started web interface and REST API at [{}].", configuration.getRestListenUri());

        // Start server.
        try {
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start REST API.", e);
        }

        initializeProbes();
    }

    public void shutdown() {
        LOG.info("Shutting down.");

        this.systemStatus.unsetStatus(SystemStatus.TYPE.RUNNING);
        this.systemStatus.setStatus(SystemStatus.TYPE.SHUTTING_DOWN);

        // Shutdown REST API.
        if (httpServer != null) {
            LOG.info("Stopping REST API.");
            httpServer.shutdownNow();
        }

        LOG.info("Shutdown complete.");
    }

    private void initializeProbes() {
        // Broad monitor probes.
        for (Dot11MonitorDefinition m : configuration.getDot11Monitors()) {
            try {
                Dot11MonitorProbe probe = new Dot11MonitorProbe(this, Dot11ProbeConfiguration.create(
                        "broad-monitor-" + m.device(),
                        configuration.getGraylogUplinks(),
                        configuration.getNzymeId(),
                        m.device(),
                        m.channels(),
                        m.channelHopInterval(),
                        m.channelHopCommand(),
                        configuration.getDot11Networks()
                ), getMetrics());

                // Add standard interceptors for broad channel monitoring.
                Dot11MonitorProbe.configureAsBroadMonitor(probe);

                // Add alerting interceptors. // TODO: load based on which alerts are activated in conf
                probe.addFrameInterceptors(new UnexpectedBSSIDInterceptorSet(probe).getInterceptors());
                probe.addFrameInterceptors(new UnexpectedSSIDInterceptorSet(probe).getInterceptors());
                probe.addFrameInterceptors(new StatisticsInterceptorSet(this).getInterceptors());


                probeExecutor.submit(probe.loop());
                this.probes.add(probe);
            } catch(Dot11ProbeInitializationException e) {
                LOG.error("Couldn't initialize probe on interface [{}].", m.device(), e);
            }
        }

        // Trap pairs.
    }

    @Override
    public AlertsService getAlertsService() {
        return alerts;
    }

    @Override
    public SystemStatus getSystemStatus() {
        return systemStatus;
    }

    @Override
    public OUIManager getOUIManager() {
        return ouiManager;
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

    @Override
    public List<Dot11Probe> getProbes() {
        return probes;
    }

    @Override
    public Networks getNetworks() {
        return networks;
    }

}
