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
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
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
import horse.wtf.nzyme.periodicals.alerting.sigindex.SignalIndexAnomalyAlertMonitor;
import horse.wtf.nzyme.periodicals.alerting.sigindex.SignalIndexCleaner;
import horse.wtf.nzyme.periodicals.alerting.sigindex.SignalIndexWriter;
import horse.wtf.nzyme.periodicals.beaconrate.BeaconRateCleaner;
import horse.wtf.nzyme.periodicals.beaconrate.BeaconRateWriter;
import horse.wtf.nzyme.periodicals.measurements.MeasurementsCleaner;
import horse.wtf.nzyme.periodicals.measurements.MeasurementsWriter;
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
import horse.wtf.nzyme.rest.resources.assets.WebInterfaceAssetsResource;
import horse.wtf.nzyme.rest.resources.system.MetricsResource;
import horse.wtf.nzyme.rest.resources.system.ProbesResource;
import horse.wtf.nzyme.rest.resources.system.StatisticsResource;
import horse.wtf.nzyme.rest.resources.system.SystemResource;
import horse.wtf.nzyme.rest.tls.SSLEngineConfiguratorBuilder;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.GZipContentEncoding;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import sun.misc.Signal;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static com.google.common.base.MoreObjects.firstNonNull;

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
        this.systemStatus = new SystemStatus();
        this.networks = new Networks(this);
        this.clients = new Clients(this);

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

        this.configuredAlerts = configuration.dot11Alerts();
        this.alerts = new AlertsService(this);

        // Disable TRAINING status when training period is over.
        LOG.info("Training period ends in <{}> seconds.", configuration.alertingTrainingPeriodSeconds());
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            LOG.info("Training period is over!");
            systemStatus.unsetStatus(SystemStatus.TYPE.TRAINING);
        }, configuration.alertingTrainingPeriodSeconds(), TimeUnit.SECONDS);

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
        periodicalManager.scheduleAtFixedRate(new MeasurementsCleaner(this), 0, 10, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new SignalIndexWriter(this), 10, 10, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new SignalIndexCleaner(this), 0, 10, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new BeaconRateWriter(this), 30, 30, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new BeaconRateCleaner(this), 0, 10, TimeUnit.MINUTES);
        if(configuration.versionchecksEnabled()) {
            periodicalManager.scheduleAtFixedRate(new VersioncheckThread(version), 0, 60, TimeUnit.MINUTES);
        } else {
            LOG.info("Versionchecks are disabled.");
        }

        if (configuredAlerts.contains(Alert.TYPE_WIDE.SIGNAL_ANOMALY)) {
            periodicalManager.scheduleAtFixedRate(new SignalIndexAnomalyAlertMonitor(this), 0, 30, TimeUnit.SECONDS);
        }

        // Spin up REST API and web interface.
        java.util.logging.Logger.getLogger("org.glassfish.grizzly").setLevel(Level.SEVERE);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new CORSFilter());
        resourceConfig.register(new InjectionBinder(this));
        resourceConfig.register(new ObjectMapperProvider());
        resourceConfig.register(new JacksonJaxbJsonProvider());
        resourceConfig.register(new NzymeExceptionMapper());

        // Register REST API resources.
        resourceConfig.register(PingResource.class);
        resourceConfig.register(AlertsResource.class);
        resourceConfig.register(ProbesResource.class);
        resourceConfig.register(MetricsResource.class);
        resourceConfig.register(StatisticsResource.class);
        resourceConfig.register(NetworksResource.class);
        resourceConfig.register(SystemResource.class);

        // Enable GZIP.
        resourceConfig.registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);

        // Register web interface asset resources.
        resourceConfig.register(WebInterfaceAssetsResource.class);

        if(configuration.useTls()) {
            try {
                httpServer = GrizzlyHttpServerFactory.createHttpServer(
                        configuration.restListenUri(),
                        resourceConfig,
                        true,
                        SSLEngineConfiguratorBuilder.build(
                                configuration.tlsCertificatePath(),
                                configuration.tlsKeyPath()
                        )
                );
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException("Could not initialize secure web server.", e);
            }
        } else {
            httpServer = GrizzlyHttpServerFactory.createHttpServer(configuration.restListenUri(), resourceConfig);
        }

        LOG.info("Started web interface and REST API at [{}]. Access it at: [{}]",
                configuration.restListenUri(),
                configuration.httpExternalUri());

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
        for (Dot11MonitorDefinition m : configuration.dot11Monitors()) {
            Dot11MonitorProbe probe = new Dot11MonitorProbe(this, Dot11ProbeConfiguration.create(
                    "broad-monitor-" + m.device(),
                    configuration.graylogUplinks(),
                    configuration.nzymeId(),
                    m.device(),
                    m.channels(),
                    m.channelHopInterval(),
                    m.channelHopCommand(),
                    configuration.dot11Networks(),
                    configuration.knownBanditFingerprints()
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
            if (configuredAlerts.contains(Alert.TYPE_WIDE.UNEXPECTED_FINGERPRINT)) {
                probe.addFrameInterceptors(new UnexpectedFingerprintInterceptorSet(probe).getInterceptors());
            }

            // Statistics interceptor.
            probe.addFrameInterceptors(new NetworksAndClientsInterceptorSet(this).getInterceptors());

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
