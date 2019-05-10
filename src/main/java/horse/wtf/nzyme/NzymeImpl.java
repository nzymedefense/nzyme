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
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.AlertsService;
import horse.wtf.nzyme.configuration.Configuration;
import horse.wtf.nzyme.configuration.Dot11MonitorDefinition;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.dot11.clients.Clients;
import horse.wtf.nzyme.dot11.interceptors.*;
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.measurements.MeasurementsCleaner;
import horse.wtf.nzyme.measurements.MeasurementsWriter;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.ouis.OUIUpdater;
import horse.wtf.nzyme.periodicals.PeriodicalManager;
import horse.wtf.nzyme.periodicals.versioncheck.VersioncheckThread;
import horse.wtf.nzyme.rest.CORSFilter;
import horse.wtf.nzyme.rest.InjectionBinder;
import horse.wtf.nzyme.rest.NzymeExceptionMapper;
import horse.wtf.nzyme.rest.ObjectMapperProvider;
import horse.wtf.nzyme.rest.resources.AlertsResource;
import horse.wtf.nzyme.rest.resources.NetworksResource;
import horse.wtf.nzyme.rest.resources.PingResource;
import horse.wtf.nzyme.rest.resources.system.MetricsResource;
import horse.wtf.nzyme.rest.resources.system.ProbesResource;
import horse.wtf.nzyme.rest.resources.system.StatisticsResource;
import horse.wtf.nzyme.rest.resources.system.SystemResource;
import horse.wtf.nzyme.statistics.Statistics;
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

    private static final Logger LOG = LogManager.getLogger(NzymeImpl.class);

    private final Configuration configuration;
    private final Database database;
    private final ExecutorService probeExecutor;
    private final Statistics statistics;
    private final MetricRegistry metrics;
    private final SystemStatus systemStatus;
    private final OUIManager ouiManager;

    private final Networks networks;
    private final Clients clients;

    private final List<Dot11Probe> probes;
    private final AlertsService alerts;

    private final List<Alert.TYPE_WIDE> configuredAlerts;

    private HttpServer httpServer;

    public NzymeImpl(Configuration configuration, Database database) {
        this.configuration = configuration;
        this.database = database;

        this.statistics = new Statistics();
        this.metrics = new MetricRegistry();
        this.probes = Lists.newArrayList();
        this.networks = new Networks(this);
        this.clients = new Clients(this);
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

        this.ouiManager = new OUIManager(this);

        this.configuredAlerts = configuration.getDot11Alerts();
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
        LOG.info("Initializing nzyme version: {}.", version.getVersionString());

        LOG.info("Active alerts: {}", configuredAlerts);

        // Initial OUI fetch. Not in periodical because this needs to be blocking.
        try {
            this.ouiManager.fetchAndUpdate();
        } catch (IOException e) {
            LOG.error("Could not initialize OUIs.", e);
        }

        // Metrics JMX reporter.
        final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();

        // Periodicals.
        PeriodicalManager periodicalManager = new PeriodicalManager();
        periodicalManager.scheduleAtFixedRate(new OUIUpdater(this), 12, 12, TimeUnit.HOURS);
        periodicalManager.scheduleAtFixedRate(new MeasurementsWriter(this), 1, 1, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new MeasurementsCleaner(this), 0, 1, TimeUnit.MINUTES);
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
        resourceConfig.register(new NzymeExceptionMapper());

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
            Dot11MonitorProbe probe = new Dot11MonitorProbe(this, Dot11ProbeConfiguration.create(
                    "broad-monitor-" + m.device(),
                    configuration.getGraylogUplinks(),
                    configuration.getNzymeId(),
                    m.device(),
                    m.channels(),
                    m.channelHopInterval(),
                    m.channelHopCommand(),
                    configuration.getDot11Networks(),
                    configuration.getKnownBanditFingerprints()
            ), getMetrics());

            // Add standard interceptors for broad channel monitoring.
            Dot11MonitorProbe.configureAsBroadMonitor(probe);

            // Add alerting interceptors.
            if (configuredAlerts.contains(Alert.TYPE_WIDE.UNEXPECTED_BSSID)) {
                probe.addFrameInterceptors(new UnexpectedBSSIDInterceptorSet(probe).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.UNEXPECTED_SSID)) {
                probe.addFrameInterceptors(new UnexpectedSSIDInterceptorSet(probe).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.CRYPTO_CHANGE)) {
                probe.addFrameInterceptors(new CryptoChangeInterceptorSet(probe).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.UNEXPECTED_CHANNEL)) {
                probe.addFrameInterceptors(new UnexpectedChannelInterceptorSet(probe).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.KNOWN_BANDIT_FINGERPRINT)) {
                probe.addFrameInterceptors(new KnownBanditFingerprintInterceptorSet(probe).getInterceptors());
            }

            // Statistics interceptor.
            probe.addFrameInterceptors(new StatisticsInterceptorSet(this).getInterceptors());

            probeExecutor.submit(probe.loop());
            this.probes.add(probe);

            // Initialization happens in thread.
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
    public Database getDatabase() {
        return database;
    }

    @Override
    public List<Dot11Probe> getProbes() {
        return probes;
    }

    @Override
    public Networks getNetworks() {
        return networks;
    }

    @Override
    public Clients getClients() {
        return clients;
    }

}
