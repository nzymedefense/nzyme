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

package app.nzyme.core;

import app.nzyme.core.distributed.ClusterManager;
import app.nzyme.core.distributed.NodeManager;
import app.nzyme.core.distributed.messaging.postgres.PostgresMessageBusImpl;
import app.nzyme.core.distributed.tasksqueue.postgres.PostgresTasksQueueImpl;
import app.nzyme.core.monitoring.health.HealthMonitor;
import app.nzyme.core.periodicals.distributed.NodeUpdater;
import app.nzyme.core.rest.server.NzymeHttpServer;
import app.nzyme.core.security.authentication.AuthenticationService;
import app.nzyme.plugin.Database;
import app.nzyme.plugin.NodeIdentification;
import app.nzyme.plugin.Plugin;
import app.nzyme.plugin.Registry;
import app.nzyme.plugin.distributed.messaging.MessageBus;
import app.nzyme.plugin.distributed.tasksqueue.TasksQueue;
import app.nzyme.plugin.retro.RetroService;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jvm.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.ConfigException;
import app.nzyme.core.alerts.Alert;
import app.nzyme.core.alerts.service.AlertsService;
import app.nzyme.core.bandits.engine.ContactManager;
import app.nzyme.core.bandits.trackers.GroundStation;
import app.nzyme.core.bandits.trackers.TrackerManager;
import app.nzyme.core.configuration.*;
import app.nzyme.core.configuration.base.BaseConfiguration;
import app.nzyme.core.configuration.node.NodeConfiguration;
import app.nzyme.core.crypto.Crypto;
import app.nzyme.core.database.DatabaseImpl;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.anonymization.Anonymizer;
import app.nzyme.core.dot11.clients.Clients;
import app.nzyme.core.dot11.deauth.DeauthenticationMonitor;
import app.nzyme.core.dot11.deception.traps.BeaconTrap;
import app.nzyme.core.dot11.deception.traps.ProbeRequestTrap;
import app.nzyme.core.dot11.deception.traps.Trap;
import app.nzyme.core.dot11.frames.Dot11Frame;
import app.nzyme.core.dot11.interceptors.*;
import app.nzyme.core.dot11.networks.sentry.Sentry;
import app.nzyme.core.dot11.probes.*;
import app.nzyme.core.dot11.networks.Networks;
import app.nzyme.core.ethernet.Ethernet;
import app.nzyme.core.notifications.Notification;
import app.nzyme.core.notifications.Uplink;
import app.nzyme.core.notifications.uplinks.UplinkFactory;
import app.nzyme.core.periodicals.alerting.beaconrate.BeaconRateAnomalyAlertMonitor;
import app.nzyme.core.periodicals.alerting.beaconrate.BeaconRateCleaner;
import app.nzyme.core.periodicals.alerting.beaconrate.BeaconRateWriter;
import app.nzyme.core.periodicals.alerting.tracks.SignalTrackMonitor;
import app.nzyme.core.ouis.OUIManager;
import app.nzyme.core.ouis.OUIUpdater;
import app.nzyme.core.periodicals.PeriodicalManager;
import app.nzyme.core.periodicals.sigidx.SignalIndexHistogramCleaner;
import app.nzyme.core.periodicals.sigidx.SignalIndexHistogramWriter;
import app.nzyme.core.periodicals.versioncheck.VersioncheckThread;
import app.nzyme.core.plugin.loading.PluginLoader;
import app.nzyme.core.processing.FrameProcessor;
import app.nzyme.core.registry.RegistryImpl;
import app.nzyme.core.remote.forwarders.Forwarder;
import app.nzyme.core.remote.forwarders.ForwarderFactory;
import app.nzyme.core.remote.inputs.RemoteFrameInput;
import app.nzyme.core.scheduler.SchedulingService;
import app.nzyme.core.systemstatus.SystemStatus;
import app.nzyme.core.tables.TablesService;
import app.nzyme.core.taps.TapManager;
import app.nzyme.core.util.MetricNames;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.quartz.SchedulerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.Key;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class NzymeNodeImpl implements NzymeNode {

    private static final Logger LOG = LogManager.getLogger(NzymeNodeImpl.class);

    private final Version version;

    private final NodeIdentification nodeIdentification;

    private final NodeConfiguration configuration;
    private final BaseConfiguration baseConfiguration;

    private final Path dataDirectory;

    private final DatabaseImpl database;
    public final AuthenticationService authenticationService;

    private final NodeManager nodeManager;
    private final ClusterManager clusterManager;
    private final NzymeHttpServer httpServer;

    private final ExecutorService probeExecutor;
    private final MetricRegistry metrics;
    private final MemoryRegistry memoryRegistry;
    private final SystemStatus systemStatus;
    private final OUIManager ouiManager;
    private final List<Uplink> uplinks;
    private final List<Forwarder> forwarders;
    private final TapManager tapManager;
    private final MessageBus messageBus;
    private final TasksQueue tasksQueue;

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

    private final HealthMonitor healthMonitor;

    private final Anonymizer anonymizer;

    private GroundStation groundStation;

    private List<String> plugins;

    private Optional<RetroService> retroService = Optional.empty();

    private final Crypto crypto;

    private final List<Object> pluginRestResources;

    private SchedulingService schedulingService;

    public NzymeNodeImpl(BaseConfiguration baseConfiguration, NodeConfiguration configuration, DatabaseImpl database) {
        this.baseConfiguration = baseConfiguration;
        this.version = new Version();
        this.dataDirectory = Path.of(baseConfiguration.dataDirectory());
        this.database = database;
        this.configuration = configuration;

        this.authenticationService = new AuthenticationService(this);

        this.nodeManager = new NodeManager(this);
        try {
            this.nodeManager.initialize();
            this.nodeIdentification = NodeIdentification.create(nodeManager.getLocalNodeId(), baseConfiguration.name());
            this.nodeManager.registerSelf();
        } catch (NodeManager.NodeInitializationException e) {
            throw new RuntimeException("Could not initialize distributed subsystem.", e);
        }

        this.clusterManager = new ClusterManager(this);
        this.messageBus = new PostgresMessageBusImpl(this);
        this.tasksQueue = new PostgresTasksQueueImpl(this);

        this.signingKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);

        this.pluginRestResources = Lists.newArrayList();
        this.plugins = Lists.newArrayList();

        this.httpServer = new NzymeHttpServer(this, this.pluginRestResources);

        this.ethernet = new Ethernet(this);

        this.uplinks = Lists.newArrayList();
        this.forwarders = Lists.newArrayList();
        this.tapManager = new TapManager(this);

        this.frameProcessor = new FrameProcessor();

        this.ignoredFingerprints = new AtomicReference<>(ImmutableList.<String>builder().build());

        this.metrics = new MetricRegistry();
        this.crypto = new Crypto(this);
        this.memoryRegistry = new MemoryRegistry();
        this.probes = Lists.newArrayList();
        this.systemStatus = new SystemStatus();
        this.networks = new Networks(this);
        this.sentry = new Sentry(this, 5);
        this.clients = new Clients(this);
        this.objectMapper = new ObjectMapper();

        this.healthMonitor = new HealthMonitor(this);

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

        LOG.info("Initializing cluster manager.");
        this.clusterManager.initialize();

        LOG.info("Initializing message bus [{}].", this.messageBus.getClass().getCanonicalName());
        this.messageBus.initialize();

        LOG.info("Initializing tasks queue [{}].", this.tasksQueue.getClass().getCanonicalName());
        this.tasksQueue.initialize();

        try {
            this.crypto.initialize();
        } catch (Crypto.CryptoInitializationException e) {
            throw new RuntimeException("Could not load cryptographic subsystem.", e);
        }

        LOG.info("Initializing authentication service.");
        this.authenticationService.initialize();

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
        UplinkFactory uplinkFactory = new UplinkFactory(nodeIdentification.name());
        for (UplinkDefinition uplinkDefinition : configuration.uplinks()) {
            registerUplink(uplinkFactory.fromConfigurationDefinition(uplinkDefinition));
        }

        // Register configured forwarders.
        ForwarderFactory forwarderFactory = new ForwarderFactory(nodeIdentification.name());
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
        periodicalManager.scheduleAtFixedRate(new NodeUpdater(this), 0, 5, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new OUIUpdater(this), 12, 12, TimeUnit.HOURS);
        periodicalManager.scheduleAtFixedRate(new BeaconRateWriter(this), 60, 60, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new BeaconRateCleaner(this), 0, 10, TimeUnit.MINUTES);
        periodicalManager.scheduleAtFixedRate(new SignalIndexHistogramWriter(this), 60, 60, TimeUnit.SECONDS);
        periodicalManager.scheduleAtFixedRate(new SignalIndexHistogramCleaner(this), 0, 10, TimeUnit.MINUTES);
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

        healthMonitor.initialize();

        // Load plugins.
        PluginLoader pl = new PluginLoader(new File(configuration.pluginDirectory())); // TODO make path configurable
        for (Plugin plugin : pl.loadPlugins()) {
            // Initialize plugin
            LOG.info("Initializing plugin of type [{}]: [{}]", plugin.getClass().getCanonicalName(), plugin.getName());

            try {
                plugin.initialize(this, getDatabaseRegistry(plugin.getId()), this, this);
            } catch(Exception e) {
                LOG.error("Could not load plugin. Skipping.", e);
                continue;
            }

            this.plugins.add(plugin.getId());
        }

        // Spin up REST API and web interface.
        java.util.logging.Logger.getLogger("org.glassfish.grizzly").setLevel(Level.SEVERE);
        java.util.logging.Logger.getLogger("org.glassfish.jersey.internal.inject.Providers").setLevel(Level.SEVERE);
        this.httpServer.initialize();

        // Ground Station.
        if (configuration.groundstationDevice() != null) {
            try {
                this.groundStation = new GroundStation(
                        Role.NODE,
                        nodeIdentification.name(),
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

    @Override
    public NodeManager getNodeManager() {
        return nodeManager;
    }

    @Override
    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    @Override
    public MessageBus getMessageBus() {
        return messageBus;
    }

    @Override
    public TasksQueue getTasksQueue() {
        return tasksQueue;
    }

    @Override
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    @Override
    public HealthMonitor getHealthMonitor() {
        return healthMonitor;
    }

    private void initializeProbes() {
        // Broad monitor probes.
        for (Dot11MonitorDefinition m : configuration.dot11Monitors()) {
            Dot11MonitorProbe probe = new Dot11MonitorProbe(Dot11ProbeConfiguration.create(
                    "broad-monitor-" + m.device(),
                    getUplinks(),
                    getNodeInformation().name(),
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
                            getNodeInformation().name(),
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

    @Override
    public List<String> getInitializedPlugins() {
        return plugins;
    }

    @Nullable
    @Override
    public Optional<RetroService> retroService() {
        return retroService;
    }

    @Override
    public Crypto getCrypto() {
        return crypto;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Override
    public NodeConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public BaseConfiguration getBaseConfiguration() {
        return baseConfiguration;
    }

    @Override
    public Path getDataDirectory() {
        return dataDirectory;
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
    public NzymeHttpServer getHttpServer() {
        return httpServer;
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
    public void registerRestResource(Object resource) {
        this.pluginRestResources.add(resource);
    }

    public Registry getDatabaseRegistry(String namespace) {
        return new RegistryImpl(this, namespace);
    }

    @Override
    public Registry getDatabaseCoreRegistry() {
        return new RegistryImpl(this, "core");
    }

    @Override
    public NodeIdentification getNodeInformation() {
        return nodeIdentification;
    }
}
