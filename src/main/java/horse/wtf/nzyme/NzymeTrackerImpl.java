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
import horse.wtf.nzyme.bandits.Bandit;
import horse.wtf.nzyme.bandits.trackers.TrackerTrackSummary;
import horse.wtf.nzyme.bandits.trackers.hid.AudioHID;
import horse.wtf.nzyme.bandits.trackers.hid.LogHID;
import horse.wtf.nzyme.bandits.trackers.hid.TextGUIHID;
import horse.wtf.nzyme.bandits.trackers.hid.TrackerHID;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.ChannelDesignator;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.TrackerBanditManager;
import horse.wtf.nzyme.bandits.trackers.GroundStation;
import horse.wtf.nzyme.bandits.trackers.trackerlogic.TrackerStateWatchdog;
import horse.wtf.nzyme.configuration.Dot11MonitorDefinition;
import horse.wtf.nzyme.configuration.base.BaseConfiguration;
import horse.wtf.nzyme.configuration.tracker.TrackerConfiguration;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.dot11.anonymization.Anonymizer;
import horse.wtf.nzyme.dot11.interceptors.BanditIdentifierInterceptorSet;
import horse.wtf.nzyme.dot11.probes.Dot11MonitorProbe;
import horse.wtf.nzyme.dot11.probes.Dot11Probe;
import horse.wtf.nzyme.dot11.probes.Dot11ProbeConfiguration;
import horse.wtf.nzyme.notifications.Notification;
import horse.wtf.nzyme.statistics.Statistics;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NzymeTrackerImpl implements NzymeTracker {

    private static final Logger LOG = LogManager.getLogger(NzymeTrackerImpl.class);

    private final Version version;

    private final String nodeID;

    private final TrackerConfiguration configuration;
    private final BaseConfiguration baseConfiguration;
    private final ExecutorService probeExecutor;
    private final GroundStation groundStation;
    private final TrackerBanditManager banditManager;
    private final TrackerStateWatchdog trackerStateWatchdog;
    private final Statistics statistics;

    private final List<Dot11Probe> probes;
    private final MetricRegistry metrics;
    private final ObjectMapper om;

    private final Anonymizer anonymizer;

    private final List<TrackerHID> hids;

    public NzymeTrackerImpl(BaseConfiguration baseConfiguration, TrackerConfiguration configuration) {
        this.version = new Version();
        this.nodeID = baseConfiguration.nodeId();
        this.configuration = configuration;
        this.baseConfiguration = baseConfiguration;

        this.anonymizer = new Anonymizer(baseConfiguration.anonymize(), baseConfiguration.dataDirectory());

        this.probes = Lists.newArrayList();
        this.hids = Lists.newArrayList();

        this.statistics = new Statistics(this);
        this.metrics = new MetricRegistry();

        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.banditManager = new TrackerBanditManager(this);

        trackerStateWatchdog = new TrackerStateWatchdog(this);
        trackerStateWatchdog.initialize();

        try {
            this.groundStation = new GroundStation(
                    Role.TRACKER,
                    baseConfiguration.nodeId(),
                    version.getVersion().toString(),
                    metrics,
                    banditManager,
                    null,
                    configuration.trackerDevice()
            );


            // Register configured HIDs.
            for (TrackerHID.TYPE type : this.configuration.hids()) {
                TrackerHID hid;
                switch (type) {
                    case AUDIO:
                        hid = new AudioHID();
                        break;
                    case LOG:
                        hid = new LogHID();
                        break;
                    case TEXTGUI:
                        hid = new TextGUIHID(this);
                        break;
                    default:
                        throw new RuntimeException("Unknown HID [" + type+ "]");
                }

                hid.initialize();
                this.hids.add(hid);
                this.groundStation.registerHID(hid);
            }
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

        this.groundStation.onPingReceived(trackerStateWatchdog::registerPing);
        this.groundStation.onStartTrackRequestReceived(banditManager::setCurrentlyTrackedBandit);
        this.groundStation.onCancelTrackRequestReceived((cancelTrackRequest ->
                banditManager.cancelTracking()
        ));

        this.banditManager.onInitialTrack(bandit -> {
            for (TrackerHID hid : hids) {
                hid.onInitialContactWithTrackedBandit(bandit);
            }
        });

        this.banditManager.onBanditTrace((bandit,rssi,channel) -> {
            for (Dot11Probe probe : getProbes()) {
                if (probe instanceof Dot11MonitorProbe) {
                    ((Dot11MonitorProbe) probe).getChannelDesignator().onBanditTrace(channel);
                }
            }

            for (TrackerHID hid : hids) {
                hid.onBanditTrace(bandit,rssi);
            }
        });

        // Send contact tracks.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setNameFormat("contact-sender-%d").build())
                .scheduleAtFixedRate(() -> {
                    if (banditManager.isCurrentlyTracking() && banditManager.hasActiveTrack()) {
                        try {
                            Bandit bandit = banditManager.getCurrentlyTrackedBandit();
                            TrackerTrackSummary trackSummary = banditManager.getTrackSummary();
                            groundStation.transmit(TrackerMessage.Wrapper.newBuilder().setContactStatus(
                                    TrackerMessage.ContactStatus.newBuilder()
                                            .setSource(baseConfiguration.nodeId())
                                            .setUuid(bandit.uuid().toString())
                                            .setRssi(trackSummary.lastSignal())
                                            .setLastSeen(trackSummary.lastContact().getMillis())
                                            .setFrames(trackSummary.frameCount())
                                            .build()).build());
                        } catch(Exception e) {
                            LOG.error("Could not send contact status.", e);
                        }
                    }
                }, 2, 10, TimeUnit.SECONDS);

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
                    baseConfiguration.nodeId(),
                    m.device(),
                    m.channels(),
                    m.channelHopInterval(),
                    m.channelHopCommand(),
                    null,
                    null
            ), metrics, statistics, anonymizer);

            probe.onChannelSwitch((previousChannel, newChannel) -> {
                for (TrackerHID hid : hids) {
                    hid.onChannelSwitch(previousChannel, newChannel);
                }

            });

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
    public String getNodeID() {
        return nodeID;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return this.om;
    }

    @Override
    public TrackerConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public BaseConfiguration getBaseConfiguration() {
        return baseConfiguration;
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
    public TrackerStateWatchdog getStateWatchdog() {
        return trackerStateWatchdog;
    }

    @Override
    public List<Dot11Probe> getProbes() {
        return probes;
    }

    @Override
    public List<TrackerHID> getHIDs() {
        return hids;
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
