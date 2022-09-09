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

package horse.wtf.nzyme;

import app.nzyme.plugin.Database;
import app.nzyme.plugin.Plugin;
import app.nzyme.plugin.Registry;
import app.nzyme.plugin.retro.RetroService;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jvm.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.ConfigException;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.alerts.ProbeFailureAlert;
import horse.wtf.nzyme.alerts.service.AlertsService;
import horse.wtf.nzyme.bandits.engine.ContactManager;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.bandits.trackers.TrackerManager;
import horse.wtf.nzyme.configuration.*;
import horse.wtf.nzyme.configuration.base.BaseConfiguration;
import horse.wtf.nzyme.configuration.db.BaseConfigurationService;
import horse.wtf.nzyme.configuration.leader.LeaderConfiguration;
import horse.wtf.nzyme.database.DatabaseImpl;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.clients.Clients;
import horse.wtf.nzyme.dot11.deauth.DeauthenticationMonitor;
import horse.wtf.nzyme.dot11.deception.traps.BeaconTrap;
import horse.wtf.nzyme.dot11.deception.traps.ProbeRequestTrap;
import horse.wtf.nzyme.dot11.deception.traps.Trap;
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import horse.wtf.nzyme.dot11.interceptors.*;
import horse.wtf.nzyme.dot11.networks.sentry.Sentry;
import horse.wtf.nzyme.dot11.probes.*;
import horse.wtf.nzyme.dot11.networks.Networks;
import horse.wtf.nzyme.ethernet.Ethernet;
import horse.wtf.nzyme.events.*;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.notifications.Uplink;
import horse.wtf.nzyme.notifications.uplinks.UplinkFactory;
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
import horse.wtf.nzyme.plugin.loading.PluginLoader;
import horse.wtf.nzyme.processing.FrameProcessor;
import horse.wtf.nzyme.registry.RegistryImpl;
import horse.wtf.nzyme.remote.forwarders.Forwarder;
import horse.wtf.nzyme.remote.forwarders.ForwarderFactory;
import horse.wtf.nzyme.remote.inputs.RemoteFrameInput;
import horse.wtf.nzyme.rest.authentication.RESTAuthenticationFilter;
import horse.wtf.nzyme.rest.authentication.TapAuthenticationFilter;
import horse.wtf.nzyme.rest.interceptors.TapTableSizeInterceptor;
import horse.wtf.nzyme.rest.resources.ethernet.DNSResource;
import horse.wtf.nzyme.rest.resources.taps.StatusResource;
import horse.wtf.nzyme.rest.resources.taps.TablesResource;
import horse.wtf.nzyme.rest.resources.taps.TapsResource;
import horse.wtf.nzyme.scheduler.SchedulingService;
import horse.wtf.nzyme.rest.CORSFilter;
import horse.wtf.nzyme.rest.NzymeLeaderInjectionBinder;
import horse.wtf.nzyme.rest.NzymeExceptionMapper;
import horse.wtf.nzyme.rest.ObjectMapperProvider;
import horse.wtf.nzyme.rest.resources.*;
import horse.wtf.nzyme.rest.resources.assets.WebInterfaceAssetsResource;
import horse.wtf.nzyme.rest.resources.authentication.AuthenticationResource;
import horse.wtf.nzyme.rest.resources.system.AssetInventoryResource;
import horse.wtf.nzyme.rest.resources.system.MetricsResource;
import horse.wtf.nzyme.rest.resources.system.ProbesResource;
import horse.wtf.nzyme.rest.resources.system.SystemResource;
import horse.wtf.nzyme.rest.tls.SSLEngineConfiguratorBuilder;
import horse.wtf.nzyme.systemstatus.SystemStatus;
import horse.wtf.nzyme.tables.TablesService;
import horse.wtf.nzyme.taps.TapManager;
import horse.wtf.nzyme.util.MetricNames;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.CompressionConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.jetbrains.annotations.Nullable;
import org.joda.time.DateTime;
import org.quartz.SchedulerException;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class NzymeLeaderImpl implements NzymeLeader {

    private static final Logger LOG = LogManager.getLogger(NzymeLeaderImpl.class);

    private final Version version;

    private final String nodeId;

    private final LeaderConfiguration configuration;
    private final DatabaseImpl database;
    private final BaseConfigurationService configurationService;
    private final ExecutorService probeExecutor;
    private final MetricRegistry metrics;
    private final MemoryRegistry memoryRegistry;
    private final SystemStatus systemStatus;
    private final EventService eventService;
    private final OUIManager ouiManager;
    private final List<Uplink> uplinks;
    private final List<Forwarder> forwarders;
    private final TapManager tapManager;

    private final Ethernet ethernet;

    private final FrameProcessor frameProcessor;

    private final AtomicReference<ImmutableList<String>> ignoredFingerprints;

    private final Networks networks;
    private final Sentry sentry;
    private final Clients clients;
    private final DeauthenticationMonitor deauthenticationMonitor;

    private final TablesService tablesService;

    private final ObjectMapper objectMapper;

    private final Key signingKey;

    private final List<Dot11Probe> probes;
    private final AlertsService alerts;
    private final ContactManager contactManager;
    private final TrackerManager trackerManager;

    private final Anonymizer anonymizer;

    private GroundStation groundStation;

    private Optional<RetroService> retroService = Optional.empty();

    private HttpServer httpServer;

    private SchedulingService schedulingService;

    public NzymeLeaderImpl(BaseConfiguration baseConfiguration, LeaderConfiguration configuration, DatabaseImpl database) {
        this.version = new Version();
        this.nodeId = baseConfiguration.nodeId();
        this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        this.configuration = configuration;
        this.database = database;

        this.ethernet = new Ethernet(this);

        this.uplinks = Lists.newArrayList();
        this.forwarders = Lists.newArrayList();
        this.configurationService = new BaseConfigurationService(this);
        this.tapManager = new TapManager(this);

        this.frameProcessor = new FrameProcessor();

        this.ignoredFingerprints = new AtomicReference<>(ImmutableList.<String>builder().build());

        this.metrics = new MetricRegistry();
        this.memoryRegistry = new MemoryRegistry();
        this.probes = Lists.newArrayList();
        this.systemStatus = new SystemStatus();
        this.eventService = new EventService(this);
        this.networks = new Networks(this);
        this.sentry = new Sentry(this, 5);
        this.clients = new Clients(this);
        this.objectMapper = new ObjectMapper();

        this.deauthenticationMonitor = new DeauthenticationMonitor(this);

        this.anonymizer = new Anonymizer(baseConfiguration.anonymize(), baseConfiguration.dataDirectory());

        try {
            this.schedulingService = new SchedulingService(this);
        } catch (SchedulerException e) {
            throw new RuntimeException("Could not instantiate scheduling service.", e);
        }

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

        this.alerts = new AlertsService(this);
        this.alerts.registerCallbacks(configuration.alertCallbacks());
        this.contactManager = new ContactManager(this);

        this.tablesService = new TablesService(this);

        this.trackerManager = new TrackerManager();

        // Register event callbacks.
        this.eventService.subscribe(Event.TYPE.BROKEN_PROBE, event -> {
            BrokenProbeEvent bpe = (BrokenProbeEvent) event;
            getAlertsService().handle(
                    ProbeFailureAlert.create(DateTime.now(), bpe.getProbeName(), bpe.getErrorDescription())
            );
        });

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

        eventService.recordEvent(new StartupEvent());

        LOG.info("Reading configuration from database.");
        this.configurationService.initialize();

        LOG.info("Active alerts: {}", configuration.dot11Alerts());

        // Initial OUI fetch. Not in periodical because this needs to be blocking.
        try {
            this.ouiManager.fetchAndUpdate();
        } catch (IOException e) {
            LOG.error("Could not initialize OUIs.", e);
        }

        // Metrics JMX reporter.
        final JmxReporter reporter = JmxReporter.forRegistry(metrics).build();
        reporter.start();

        // Database metrics.
        metrics.register(MetricNames.DATABASE_SIZE, (Gauge<Long>) database::getTotalSize);

        // Register configured uplinks.
        UplinkFactory uplinkFactory = new UplinkFactory(getNodeID());
        for (UplinkDefinition uplinkDefinition : configuration.uplinks()) {
            registerUplink(uplinkFactory.fromConfigurationDefinition(uplinkDefinition));
        }

        // Register configured forwarders.
        ForwarderFactory forwarderFactory = new ForwarderFactory(getNodeID());
        for (ForwarderDefinition forwarderDefinition : configuration.forwarders()) {
            this.forwarders.add(forwarderFactory.fromConfigurationDefinition(forwarderDefinition));
        }

        // Start remote input if enabled.
        if (configuration.remoteInputAddress() != null) {
            RemoteFrameInput input = new RemoteFrameInput(this, configuration.remoteInputAddress());
            Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("remote-input-%d")
                    .build())
                    .submit(input.run());
        }

        // Scheduler.
        try {
            schedulingService.initialize();
        } catch (SchedulerException e) {
            throw new RuntimeException("Could not start scheduling service.", e);
        }

        // Periodicals. (TODO: Replace with scheduler service)
        PeriodicalManager periodicalManager = new PeriodicalManager();
        periodicalManager.scheduleAtFixedRate(new OUIUpdater(this), 12, 12, TimeUnit.HOURS);
        periodicalManager.scheduleAtFixedRate(new MeasurementsWriter(this), 1, 1, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new MeasurementsCleaner(this), 0, 10, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new BeaconRateWriter(this), 60, 60, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new BeaconRateCleaner(this), 0, 10, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new SignalIndexHistogramWriter(this), 60, 60, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new SignalIndexHistogramCleaner(this), 0, 10, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new ProbeStatusMonitor(this), 1, 1, TimeUnit.MINUTES);
        if(configuration.versionchecksEnabled()) {
            periodicalManager.scheduleAtFixedRate(new VersioncheckThread(version, this), 0, 60, TimeUnit.MINUTES);
        } else {
            LOG.info("Versionchecks are disabled.");
        }

        if (configuration.dot11Alerts().contains(Alert.TYPE_WIDE.BEACON_RATE_ANOMALY)) {
            periodicalManager.scheduleAtFixedRate(new BeaconRateAnomalyAlertMonitor(this), 60, 60, TimeUnit.SECONDS);
        }

        if(configuration.dot11Alerts().contains(Alert.TYPE_WIDE.MULTIPLE_SIGNAL_TRACKS)) {
            periodicalManager.scheduleAtFixedRate(new SignalTrackMonitor(this), 60, 60, TimeUnit.SECONDS);
        }

        // Spin up REST API and web interface.
        java.util.logging.Logger.getLogger("org.glassfish.grizzly").setLevel(Level.SEVERE);
        ResourceConfig resourceConfig = new ResourceConfig();
        resourceConfig.register(new RESTAuthenticationFilter(this));
        resourceConfig.register(new TapAuthenticationFilter(this));
        resourceConfig.register(new CORSFilter());
        resourceConfig.register(new NzymeLeaderInjectionBinder(this));
        resourceConfig.register(new ObjectMapperProvider());
        resourceConfig.register(new JacksonJaxbJsonProvider());
        resourceConfig.register(new NzymeExceptionMapper());
        resourceConfig.register(new TapTableSizeInterceptor(this));

        // Register REST API resources.
        resourceConfig.register(AuthenticationResource.class);
        resourceConfig.register(PingResource.class);
        resourceConfig.register(AlertsResource.class);
        resourceConfig.register(BanditsResource.class);
        resourceConfig.register(ProbesResource.class);
        resourceConfig.register(TrackersResource.class);
        resourceConfig.register(MetricsResource.class);
        resourceConfig.register(NetworksResource.class);
        resourceConfig.register(SystemResource.class);
        resourceConfig.register(DashboardResource.class);
        resourceConfig.register(AssetInventoryResource.class);
        resourceConfig.register(ReportsResource.class);
        resourceConfig.register(StatusResource.class);
        resourceConfig.register(TablesResource.class);
        resourceConfig.register(TapsResource.class);
        resourceConfig.register(DNSResource.class);

        // Enable GZIP.
        resourceConfig.registerClasses(EncodingFilter.class, GZipEncoder.class, DeflateEncoder.class);

        // Register web interface asset resources.
        resourceConfig.register(WebInterfaceAssetsResource.class);

        try {
            if (configuration.useTls()) {
                httpServer = GrizzlyHttpServerFactory.createHttpServer(
                        configuration.restListenUri(),
                        resourceConfig,
                        true,
                        SSLEngineConfiguratorBuilder.build(
                                configuration.tlsCertificatePath(),
                                configuration.tlsKeyPath()
                        )
                );
            } else {
                httpServer = GrizzlyHttpServerFactory.createHttpServer(configuration.restListenUri(), resourceConfig);
            }
        } catch(Exception e) {
            throw new RuntimeException("Could not start web server.", e);
        }

        CompressionConfig compressionConfig = httpServer.getListener("grizzly").getCompressionConfig();
        compressionConfig.setCompressionMode(CompressionConfig.CompressionMode.ON);
        compressionConfig.setCompressionMinSize(1);
        compressionConfig.setCompressibleMimeTypes();

        // Load plugins.
        PluginLoader pl = new PluginLoader(new File("plugin/")); // TODO make path configurable
        for (Plugin plugin : pl.loadPlugins()) {
            // Initialize plugin
            LOG.info("Initializing plugin of type [{}]: [{}]", plugin.getClass().getCanonicalName(), plugin.getName());
            plugin.initialize(this, this);
        }


        // Start server.
        try {
            httpServer.start();
        } catch (IOException e) {
            throw new RuntimeException("Could not start REST API.", e);
        }

        LOG.info("Started web interface and REST API at [{}]. Access it at: [{}]",
                configuration.restListenUri(),
                configuration.httpExternalUri());

        // Ground Station.
        if (configuration.groundstationDevice() != null) {
            try {
                this.groundStation = new GroundStation(
                        Role.LEADER,
                        getNodeID(),
                        version.getVersion().toString(),
                        metrics,
                        contactManager,
                        trackerManager,
                        configuration.groundstationDevice()
                );

                // Pings.
                this.groundStation.onPingReceived(this.trackerManager::registerTrackerPing);

                // Contact status updates.
                this.groundStation.onContactStatusReceived(contactManager::registerTrackerContactStatus);
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
    }

    public void shutdown() {
        LOG.info("Shutting down.");

        this.systemStatus.unsetStatus(SystemStatus.TYPE.RUNNING);
        this.systemStatus.setStatus(SystemStatus.TYPE.SHUTTING_DOWN);

        eventService.recordEvent(new ShutdownEvent());

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
                    getUplinks(),
                    getNodeID(),
                    m.device(),
                    m.channels(),
                    m.channelHopInterval(),
                    m.channelHopCommand(),
                    m.skipEnableMonitor(),
                    m.maxIdleTimeSeconds(),
                    configuration.dot11Networks(),
                    configuration.dot11TrapDevices()
            ), frameProcessor, metrics, anonymizer, this,false);

            probeExecutor.submit(probe.loop());
            this.probes.add(probe);

            // Initialization happens in thread.
        }

        // Broad monitor interceptors.
        frameProcessor.registerDot11Interceptors(new BroadMonitorInterceptorSet(this).getInterceptors());

        // Bandit interceptors.
        frameProcessor.registerDot11Interceptors(new BanditIdentifierInterceptorSet(getContactManager()).getInterceptors());

        // Sentry interceptors.
        frameProcessor.registerDot11Interceptors(
                new SentryInterceptorSet(sentry, alerts, configuration.dot11Alerts().contains(Alert.TYPE_WIDE.UNKNOWN_SSID)).getInterceptors()
        );

        // Deauth counter.
        frameProcessor.registerDot11Interceptors(new DeauthFrameCounterInterceptorSet(deauthenticationMonitor).getInterceptors());

        // Dot11 alerting interceptors.
        if (configuration.dot11Alerts().contains(Alert.TYPE_WIDE.UNEXPECTED_BSSID)) {
            frameProcessor.registerDot11Interceptors(new UnexpectedBSSIDInterceptorSet(getAlertsService(), configuration.dot11Networks()).getInterceptors());
        }
        if (configuration.dot11Alerts().contains(Alert.TYPE_WIDE.UNEXPECTED_SSID)) {
            frameProcessor.registerDot11Interceptors(new UnexpectedSSIDInterceptorSet(getAlertsService(), configuration.dot11Networks()).getInterceptors());
        }
        if (configuration.dot11Alerts().contains(Alert.TYPE_WIDE.CRYPTO_CHANGE)) {
            frameProcessor.registerDot11Interceptors(new CryptoChangeInterceptorSet(getAlertsService(), configuration.dot11Networks()).getInterceptors());
        }
        if (configuration.dot11Alerts().contains(Alert.TYPE_WIDE.UNEXPECTED_CHANNEL)) {
            frameProcessor.registerDot11Interceptors(new UnexpectedChannelInterceptorSet(getAlertsService(), configuration.dot11Networks()).getInterceptors());
        }
        if (configuration.dot11Alerts().contains(Alert.TYPE_WIDE.UNEXPECTED_FINGERPRINT)) {
            frameProcessor.registerDot11Interceptors(new UnexpectedFingerprintInterceptorSet(getAlertsService(), configuration.dot11Networks()).getInterceptors());
        }
        if (configuration.dot11Alerts().contains(Alert.TYPE_WIDE.PWNAGOTCHI_ADVERTISEMENT)) {
            frameProcessor.registerDot11Interceptor(new PwnagotchiAdvertisementInterceptor(getAlertsService()));
        }


        // Traps.
        for (Dot11TrapDeviceDefinition td : configuration.dot11TrapDevices()) {
            Dot11TrapConfiguration tc = td.trap();

            // This part doesn't belong here but it's fine for now. Probably want a factory. TODO REFACTOR.
            Trap trap;
            try {
                switch (tc.type()) {
                    case PROBE_REQUEST_1:
                        trap = new ProbeRequestTrap(
                                this,
                                td.device(),
                                tc.configuration().getStringList(ConfigurationKeys.SSIDS),
                                tc.configuration().getString(ConfigurationKeys.TRANSMITTER),
                                tc.configuration().getInt(ConfigurationKeys.DELAY_SECONDS)
                        );
                        break;
                    case BEACON_1:
                        trap = new BeaconTrap(
                                this,
                                td.device(),
                                tc.configuration().getStringList(ConfigurationKeys.SSIDS),
                                tc.configuration().getString(ConfigurationKeys.TRANSMITTER),
                                tc.configuration().getInt(ConfigurationKeys.DELAY_MILLISECONDS),
                                tc.configuration().getString(ConfigurationKeys.FINGERPRINT)
                        );
                        break;
                    default:
                        LOG.error("Cannot construct trap of type [{}]. Unknown type. Skipping.", tc.type());
                        continue;
                }

                trap.checkConfiguration();
            } catch(ConfigException e) {
                LOG.error("Invalid configuration for trap of type [{}]. Skipping.", tc.type(), e);
                continue;
            } catch (Exception e) {
                LOG.error("Failed to construct trap of type [{}]. Skipping.", tc.type(), e);
                continue;
            }

            // Register interceptors of this trap.
            LOG.info("Registering frame interceptors of [{}].", trap.getClass().getCanonicalName());
            frameProcessor.registerDot11Interceptors(trap.requestedInterceptors());

            // Start probe.
            Dot11SenderProbe probe = new Dot11SenderProbe(
                    Dot11ProbeConfiguration.create(
                            "trap-sender-" + td.device() + "-" + tc.type(),
                            getUplinks(),
                            getNodeID(),
                            td.device(),
                            ImmutableList.copyOf(td.channels()),
                            td.channelHopInterval(),
                            td.channelHopCommand(),
                            td.skipEnableMonitor(),
                            60,
                            configuration.dot11Networks(),
                            configuration.dot11TrapDevices()
                    ), trap, metrics);

            trap.setProbe(probe);

            probeExecutor.submit(probe.loop());
            probes.add(probe);
        }
    }

    @Override
    public String getNodeID() {
        return nodeId;
    }

    @Override
    public Ethernet getEthernet() {
        return ethernet;
    }

    @Override
    public FrameProcessor getFrameProcessor() {
        return this.frameProcessor;
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
    public TapManager getTapManager() {
        return tapManager;
    }

    @Override
    public List<String> getIgnoredFingerprints() {
        return ignoredFingerprints.get();
    }

    @Override
    public synchronized void registerIgnoredFingerprint(String fingerprint) {
        ignoredFingerprints.set(
                new ImmutableList.Builder<String>()
                        .addAll(ignoredFingerprints.get())
                        .add(fingerprint)
                        .build()
        );
    }

    @Override
    public TablesService getTablesService() {
        return tablesService;
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
    public EventService getEventService() {
        return eventService;
    }

    @Override
    public SchedulingService getSchedulingService() {
        return schedulingService;
    }

    @Override
    public OUIManager getOUIManager() {
        return ouiManager;
    }

    @Override
    public Anonymizer getAnonymizer() {
        return anonymizer;
    }

    @Nullable
    @Override
    public Optional<RetroService> retroService() {
        return retroService;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public LeaderConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public BaseConfigurationService getConfigurationService() {
        return configurationService;
    }

    @Override
    public MetricRegistry getMetrics() {
        return metrics;
    }

    @Override
    public MemoryRegistry getRegistry() {
        return memoryRegistry;
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
    public Sentry getSentry() {
        return sentry;
    }

    @Override
    public Clients getClients() {
        return clients;
    }

    public ImmutableList<Uplink> getUplinks() {
        return ImmutableList.copyOf(this.uplinks);
    }

    @Override
    public void registerUplink(Uplink uplink) {
        LOG.info("Registering uplink of type [{}].", uplink.getClass().getCanonicalName());
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
    public void forwardFrame(Dot11Frame frame) {
        for (Forwarder forwarder : this.forwarders) {
            forwarder.forward(frame);
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

    @Override
    public void registerRetroService(RetroService service) {
        if (this.retroService.isPresent()) {
            LOG.error("Attempt to register a new RetroService but one already exists. Aborting.");
            return;
        }

        this.retroService = Optional.of(service);
    }

    @Override
    public Registry getRegistry(String namespace) {
        return new RegistryImpl(this, namespace);
    }

}
