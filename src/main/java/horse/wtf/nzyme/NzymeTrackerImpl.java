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
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.alerts.Alert;
import horse.wtf.nzyme.bandits.trackers.hid.LogHID;
import horse.wtf.nzyme.bandits.trackers.hid.TextGUIHID;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.TrackerBanditManager;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.bandits.trackers.hid.AudioHID;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.TrackerStateWatchdog;
import horse.wtf.nzyme.configuration.Dot11MonitorDefinition;
import horse.wtf.nzyme.configuration.tracker.TrackerConfiguration;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.interceptors.BanditIdentifierInterceptorSet;
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.statistics.Statistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NzymeTrackerImpl implements NzymeTracker {

    private static final Logger LOG = LogManager.getLogger(NzymeTrackerImpl.class);

    private final Version version;

    private final TrackerConfiguration configuration;
    private final ExecutorService probeExecutor;
    private final GroundStation groundStation;
    private final TrackerBanditManager banditManager;
    private final Statistics statistics;

    private final List<Dot11Probe> probes;
    private final MetricRegistry metrics;
    private final ObjectMapper om;

    public NzymeTrackerImpl(TrackerConfiguration configuration) {
        this.version = new Version();
        this.configuration = configuration;

        this.probes = Lists.newArrayList();

        this.statistics = new Statistics(this);
        this.metrics = new MetricRegistry();

        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.banditManager = new TrackerBanditManager(this);

        try {
            this.groundStation = new GroundStation(
                    Role.TRACKER,
                    configuration.nzymeId(),
                    version.getVersion().toString(),
                    metrics,
                    banditManager,
                    null,
                    configuration.trackerDevice()
            );
            AudioHID audioHID = new AudioHID();
            audioHID.initialize();
            LogHID logHid = new LogHID();
            logHid.initialize();
            TextGUIHID textGUIHID = new TextGUIHID();
            textGUIHID.initialize();

            this.groundStation.registerHID(audioHID);
            this.groundStation.registerHID(logHid);
            this.groundStation.registerHID(textGUIHID);
        } catch(Exception e) {
            throw new RuntimeException("Tracker Device configuration failed.", e);
        }

        probeExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("probe-loop-%d")
                .build());
    }

    @Override
    public void initialize() {
        LOG.info("Initializing nzyme tracker version: {}.", version.getVersionString());

        TrackerStateWatchdog trackerStateWatchdog = new TrackerStateWatchdog(this);
        trackerStateWatchdog.initialize();

        this.groundStation.onPingReceived(trackerStateWatchdog::registerPing);
        this.groundStation.onBanditBroadcastReceived(banditManager::registerBandit);
        this.groundStation.onStartTrackRequestReceived((startTrackRequest ->
                banditManager.setCurrentlyTrackedBandit(UUID.fromString(startTrackRequest.getUuid())))
        );
        this.groundStation.onCancelTrackRequestReceived((cancelTrackRequest ->
                banditManager.cancelTracking()
        ));

        // Send contact tracks.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat("contact-sender-%d").build())
                .scheduleAtFixedRate(() -> {
                    if (banditManager.isCurrentlyTracking() && banditManager.hasActiveTrack()) {
                        //groundStation.transmit(); // TODO
                    }
                }, 0, 10, TimeUnit.SECONDS);

        Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("ground-station-%d")
                        .build())
                .submit(groundStation);

        // Probes.
        for (Dot11MonitorDefinition m : configuration.dot11Monitors()) {
            Dot11MonitorProbe probe = new Dot11MonitorProbe(Dot11ProbeConfiguration.create(
                    "broad-monitor-" + m.device(),
                    null,
                    configuration.nzymeId(),
                    m.device(),
                    m.channels(),
                    m.channelHopInterval(),
                    m.channelHopCommand(),
                    null,
                    null
            ), metrics, statistics);

            // Register the bandit interceptor.
            probe.addFrameInterceptors(new BanditIdentifierInterceptorSet(getBanditManager()).getInterceptors());

            probeExecutor.submit(probe.loop());
            this.probes.add(probe);
        }
    }

    @Override
    public void shutdown() {
        this.groundStation.stop();
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.om;
    }

    @Override
    public GroundStation getGroundStation() {
        return groundStation;
    }

    @Override
    public TrackerBanditManager getBanditManager() {
        return banditManager;
    }

    @Override
    public MetricRegistry getMetrics() {
        return metrics;
    }

    @Override
    public void notifyUplinks(Notification notification, Dot11MetaInformation meta) {
        // ignored for tracker
    }

    @Override
    public void notifyUplinksOfAlert(Alert alert) {
        // ignored for tracker
    }
}
