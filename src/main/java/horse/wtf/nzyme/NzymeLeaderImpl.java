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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.Contact;
import horse.wtf.nzyme.bandits.engine.ContactManager;
import horse.wtf.nzyme.bandits.trackers.BanditListBroadcaster;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.bandits.trackers.TrackerManager;
import horse.wtf.nzyme.bandits.trackers.messagehandlers.ContactStatusMessageHandler;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.configuration.*;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.database.Database;
import horse.wtf.nzyme.debug.LeaderDebug;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.clients.Clients;
import horse.wtf.nzyme.dot11.deception.traps.ProbeRequestTrap;
import horse.wtf.nzyme.dot11.deception.traps.Trap;
import horse.wtf.nzyme.dot11.interceptors.*;
import horse.wtf.nzyme.dot11.probes.*;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogAddress;
import horse.wtf.nzyme.notifications.uplinks.graylog.GraylogUplink;
import horse.wtf.nzyme.periodicals.alerting.beaconrate.BeaconRateAnomalyAlertMonitor;
import horse.wtf.nzyme.periodicals.alerting.beaconrate.BeaconRateCleaner;
import horse.wtf.nzyme.periodicals.alerting.beaconrate.BeaconRateWriter;
import horse.wtf.nzyme.periodicals.alerting.tracks.SignalTrackMonitor;
import horse.wtf.nzyme.periodicals.measurements.MeasurementsCleaner;
import horse.wtf.nzyme.periodicals.measurements.MeasurementsWriter;
import horse.wtf.nzyme.ouis.OUIManager;
import horse.wtf.nzyme.ouis.OUIUpdater;
import horse.wtf.nzyme.periodicals.PeriodicalManager;
import horse.wtf.nzyme.periodicals.sigidx.SignalIndexHistogramCleaner;
import horse.wtf.nzyme.periodicals.sigidx.SignalIndexHistogramWriter;
import horse.wtf.nzyme.periodicals.versioncheck.VersioncheckThread;
import horse.wtf.nzyme.rest.CORSFilter;
import horse.wtf.nzyme.rest.InjectionBinder;
import horse.wtf.nzyme.rest.NzymeExceptionMapper;
import horse.wtf.nzyme.rest.ObjectMapperProvider;
import horse.wtf.nzyme.rest.authentication.AuthenticationFilter;
import horse.wtf.nzyme.rest.resources.*;
import horse.wtf.nzyme.rest.resources.assets.WebInterfaceAssetsResource;
import horse.wtf.nzyme.rest.resources.authentication.AuthenticationResource;
import horse.wtf.nzyme.rest.resources.system.MetricsResource;
import horse.wtf.nzyme.rest.resources.system.ProbesResource;
import horse.wtf.nzyme.rest.resources.system.StatisticsResource;
import horse.wtf.nzyme.rest.resources.system.SystemResource;
import horse.wtf.nzyme.rest.tls.SSLEngineConfiguratorBuilder;
import horse.wtf.nzyme.statistics.Statistics;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.joda.time.DateTime;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class NzymeLeaderImpl implements NzymeLeader {

    private static final Logger LOG = LogManager.getLogger(NzymeLeaderImpl.class);

    private final Version version;

    private final LeaderConfiguration configuration;
    private final Database database;
    private final ExecutorService probeExecutor;
    private final Statistics statistics;
    private final MetricRegistry metrics;
    private final Registry registry;
    private final SystemStatus systemStatus;
    private final OUIManager ouiManager;
    private final List<Uplink> uplinks;

    private final Networks networks;
    private final Clients clients;

    private final ObjectMapper objectMapper;

    private final Key signingKey;

    private final List<Dot11Probe> probes;
    private final AlertsService alerts;
    private final ContactManager contactManager;
    private final TrackerManager trackerManager;

    private GroundStation groundStation;

    private final List<Alert.TYPE_WIDE> configuredAlerts;

    private HttpServer httpServer;

    public NzymeLeaderImpl(LeaderConfiguration configuration, Database database) {
        this.version = new Version();
        this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        this.configuration = configuration;
        this.database = database;
        this.uplinks = Lists.newArrayList();

        this.statistics = new Statistics(this);
        this.metrics = new MetricRegistry();
        this.registry = new Registry();
        this.probes = Lists.newArrayList();
        this.systemStatus = new SystemStatus();
        this.networks = new Networks(this);
        this.clients = new Clients(this);
        this.objectMapper = new ObjectMapper();

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
        this.contactManager = new ContactManager(this);

        this.trackerManager = new TrackerManager();

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

        // Register configured Graylog uplinks.
        for (GraylogAddress graylogAddress : configuration.graylogUplinks()) {
            registerUplink(new GraylogUplink(graylogAddress.host(), graylogAddress.port(), configuration.nzymeId()));
        }

        // Periodicals.
        PeriodicalManager periodicalManager = new PeriodicalManager();
        periodicalManager.scheduleAtFixedRate(new OUIUpdater(this), 12, 12, TimeUnit.HOURS);
        periodicalManager.scheduleAtFixedRate(new MeasurementsWriter(this), 1, 1, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new MeasurementsCleaner(this), 0, 10, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new BeaconRateWriter(this), 60, 60, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new BeaconRateCleaner(this), 0, 10, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new SignalIndexHistogramWriter(this), 60, 60, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new SignalIndexHistogramCleaner(this), 0, 10, TimeUnit.MINUTES);
        if(configuration.versionchecksEnabled()) {
            periodicalManager.scheduleAtFixedRate(new VersioncheckThread(version, this), 0, 60, TimeUnit.MINUTES);
        } else {
            LOG.info("Versionchecks are disabled.");
        }

        if (configuredAlerts.contains(Alert.TYPE_WIDE.BEACON_RATE_ANOMALY)) {
            periodicalManager.scheduleAtFixedRate(new BeaconRateAnomalyAlertMonitor(this), 60, 60, TimeUnit.SECONDS);
        }

        if(configuredAlerts.contains(Alert.TYPE_WIDE.MULTIPLE_SIGNAL_TRACKS)) {
            periodicalManager.scheduleAtFixedRate(new SignalTrackMonitor(this), 60, 60, TimeUnit.SECONDS);
        }

        // Spin up REST API and web interface.
        java.util.logging.Logger.getLogger("org.glassfish.grizzly").setLevel(Level.SEVERE);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new AuthenticationFilter(this));
        resourceConfig.register(new CORSFilter());
        resourceConfig.register(new InjectionBinder(this));
        resourceConfig.register(new ObjectMapperProvider());
        resourceConfig.register(new JacksonJaxbJsonProvider());
        resourceConfig.register(new NzymeExceptionMapper());

        // Register REST API resources.
        resourceConfig.register(AuthenticationResource.class);
        resourceConfig.register(PingResource.class);
        resourceConfig.register(AlertsResource.class);
        resourceConfig.register(BanditsResource.class);
        resourceConfig.register(ProbesResource.class);
        resourceConfig.register(TrackersResource.class);
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

        // Ground Station.
        if (configuration.trackerDevice() != null) {
            try {
                this.groundStation = new GroundStation(
                        Role.LEADER,
                        configuration.nzymeId(),
                        version.getVersion().toString(),
                        metrics,
                        contactManager,
                        trackerManager,
                        configuration.trackerDevice()
                );

                // Pings.
                this.groundStation.onPingReceived(this.trackerManager::registerTrackerPing);

                // Contact status updates.
                this.groundStation.onContactStatusReceived(contactManager::registerTrackerContactStatus);

                new BanditListBroadcaster(this).initialize();
            } catch(Exception e) {
                throw new RuntimeException("Tracker Device configuration failed.", e);
            }

            Executors.newSingleThreadExecutor(
                    new ThreadFactoryBuilder()
                            .setDaemon(true)
                            .setNameFormat("ground-station-%d")
                            .build())
                    .submit(() -> groundStation.run());
        }

        // Initialize probes.
        initializeProbes();

        // Initialize all debug methods.
        new LeaderDebug(this).initialize();
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

        if (this.groundStation != null) {
            LOG.info("Stopping Ground Station.");
            this.groundStation.stop();
        }

        LOG.info("Shutdown complete.");
    }

    private void initializeProbes() {
        // Broad monitor probes.
        for (Dot11MonitorDefinition m : configuration.dot11Monitors()) {
            Dot11MonitorProbe probe = new Dot11MonitorProbe(Dot11ProbeConfiguration.create(
                    "broad-monitor-" + m.device(),
                    configuration.graylogUplinks(),
                    configuration.nzymeId(),
                    m.device(),
                    m.channels(),
                    m.channelHopInterval(),
                    m.channelHopCommand(),
                    configuration.dot11Networks(),
                    configuration.dot11TrapDevices()
            ), metrics, statistics);

            // Add standard interceptors for broad channel monitoring.
            Dot11MonitorProbe.configureAsBroadMonitor(probe, this);

            // Add alerting interceptors.
            if (configuredAlerts.contains(Alert.TYPE_WIDE.UNEXPECTED_BSSID)) {
                probe.addFrameInterceptors(new UnexpectedBSSIDInterceptorSet(alerts, configuration.dot11Networks()).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.UNEXPECTED_SSID)) {
                probe.addFrameInterceptors(new UnexpectedSSIDInterceptorSet(alerts, configuration.dot11Networks()).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.CRYPTO_CHANGE)) {
                probe.addFrameInterceptors(new CryptoChangeInterceptorSet(alerts, configuration.dot11Networks()).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.UNEXPECTED_CHANNEL)) {
                probe.addFrameInterceptors(new UnexpectedChannelInterceptorSet(alerts, configuration.dot11Networks()).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.UNEXPECTED_FINGERPRINT)) {
                probe.addFrameInterceptors(new UnexpectedFingerprintInterceptorSet(alerts, configuration.dot11Networks()).getInterceptors());
            }
            if (configuredAlerts.contains(Alert.TYPE_WIDE.PWNAGOTCHI_ADVERTISEMENT)) {
                probe.addFrameInterceptor(new PwnagotchiAdvertisementInterceptor(alerts));
            }

            // Networks manager interceptors.
            probe.addFrameInterceptors(new NetworksAndClientsInterceptorSet(this).getInterceptors());

            // Bandit identifier.
            probe.addFrameInterceptors(new BanditIdentifierInterceptorSet(getContactManager()).getInterceptors());

            probeExecutor.submit(probe.loop());
            this.probes.add(probe);

            // Initialization happens in thread.
        }

        // Traps.
        for (Dot11TrapDeviceDefinition td : configuration.dot11TrapDevices()) {
            int i = 0;
            for (Dot11TrapConfiguration tc : td.traps()) {
                Trap trap;

                // This part doesn't belong here but it's fine for now. REFACTOR.
                switch (tc.type()) {
                    case PROBE_REQUEST_1:
                        // TODO validation
                        try {
                            trap = new ProbeRequestTrap(
                                    configuration,
                                    td.deviceSender(),
                                    tc.configuration().getStringList(ConfigurationKeys.SSIDS),
                                    tc.configuration().getString(ConfigurationKeys.TRANSMITTER),
                                    tc.configuration().getInt(ConfigurationKeys.DELAY_SECONDS)
                            );
                        } catch(Exception e) {
                            LOG.error("Failed to construct trap of type [{}].", tc.type(), e);
                            continue;
                        }
                        break;
                    default:
                        LOG.error("Cannot construct trap of type [{}]", tc.type());
                        continue;
                }

                Dot11SenderProbe probe = new Dot11SenderProbe(
                        Dot11ProbeConfiguration.create(
                                "trap-sender-" + td.deviceSender() + "-" + tc.type() + "-" + i,
                                configuration.graylogUplinks(),
                                configuration.nzymeId(),
                                td.deviceSender(),
                                td.channels(),
                                td.channelHopInterval(),
                                td.channelHopCommand(),
                                configuration.dot11Networks(),
                                configuration.dot11TrapDevices()
                        ), trap, statistics, metrics);

                probeExecutor.submit(probe.loop());
                probes.add(probe);

                i++;
            }
        }

        // TODO: Trap interceptors on all monitors.
    }

    @Override
    public AlertsService getAlertsService() {
        return alerts;
    }

    @Override
    public ContactManager getContactManager() {
        return contactManager;
    }

    @Override
    public TrackerManager getTrackerManager() {
        return trackerManager;
    }

    @Override
    public GroundStation getGroundStation() {
        return groundStation;
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
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public Statistics getStatistics() {
        return statistics;
    }

    @Override
    public LeaderConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public MetricRegistry getMetrics() {
        return metrics;
    }

    @Override
    public Registry getRegistry() {
        return registry;
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

    @Override
    public void registerUplink(Uplink uplink) {
        this.uplinks.add(uplink);
    }

    @Override
    public void notifyUplinks(Notification notification, Dot11MetaInformation meta) {
        for (Uplink uplink : uplinks) {
            uplink.notify(notification, meta);
        }
    }

    @Override
    public void notifyUplinksOfAlert(Alert alert) {
        for (Uplink uplink : uplinks) {
            uplink.notifyOfAlert(alert);
        }
    }

    @Override
    public Key getSigningKey() {
        return signingKey;
    }

    @Override
    public Version getVersion() {
        return version;
    }

}
