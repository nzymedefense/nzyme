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

package horse.wtf.nzyme.dot11.networks;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11TaggedParameters;
import horse.wtf.nzyme.dot11.frames.*;
import horse.wtf.nzyme.dot11.networks.sigindex.SignalIndexManager;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Networks {

    private static final Logger LOG = LogManager.getLogger(Networks.class);

    private final Map<String, BSSID> bssids;
    private final SignalIndexManager signalIndexManager;
    private final Nzyme nzyme;

    private final Timer tableCleanerTimer;

    public Networks(Nzyme nzyme) {
        this.nzyme = nzyme;
        this.bssids = Maps.newHashMap();
        this.signalIndexManager = new SignalIndexManager(nzyme);

        this.tableCleanerTimer = nzyme.getMetrics().timer(MetricRegistry.name(MetricNames.SIGNAL_INDEX_MEMORY_CLEANER_TIMER));

        if (!nzyme.getMetrics().getGauges().containsKey(MetricNames.NETWORKS_SIGNAL_QUALITY_MEASUREMENTS)) {
            nzyme.getMetrics().register(MetricNames.NETWORKS_SIGNAL_QUALITY_MEASUREMENTS, (Gauge<Long>) () -> {
                long result = 0;
                for (BSSID bssid : bssids.values()) {
                    for (SSID ssid : bssid.ssids().values()) {
                        for (Channel channel : ssid.channels().values()) {
                            result += channel.recentSignalQuality().size();
                        }
                    }
                }
                return result;
            });
        }

        if (!nzyme.getMetrics().getGauges().containsKey(MetricNames.NETWORKS_DELTA_STATE_MEASUREMENTS)) {
            nzyme.getMetrics().register(MetricNames.NETWORKS_DELTA_STATE_MEASUREMENTS, (Gauge<Long>) () -> {
                long result = 0;
                for (BSSID bssid : bssids.values()) {
                    for (SSID ssid : bssid.ssids().values()) {
                        for (Channel channel : ssid.channels().values()) {
                            result += channel.recentDeltaStates().size();
                        }
                    }
                }
                return result;
            });
        }

        // Regularly delete networks that have not been seen for a while.
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("bssids-cleaner")
                        .build()
        ).scheduleAtFixedRate(() -> retentionClean(600), 1, 1, TimeUnit.MINUTES);

        // Regularly delete old channel signal quality measurements.
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("channels-sigqual-cleaner")
                        .build()
        ).scheduleAtFixedRate(() -> {
            Timer.Context ctx = this.tableCleanerTimer.time();
            try {
                for (BSSID bssid : bssids.values()) {
                    for (SSID ssid : bssid.ssids().values()) {
                        for (Channel channel : ssid.channels().values()) {
                            DateTime cutoff = DateTime.now().minusMinutes(1);
                            LOG.debug("Deleting expired signal quality measurements from [{}]", channel);

                            ImmutableList.Builder<Channel.SignalQuality> newQualities = new ImmutableList.Builder<>();
                            channel.recentSignalQuality().forEach(signalQuality -> {
                                if (signalQuality.createdAt().isAfter(cutoff)) {
                                    newQualities.add(signalQuality);
                                }
                            });
                            channel.recentSignalQuality().clear();
                            channel.recentSignalQuality().addAll(newQualities.build());

                            ImmutableList.Builder<Channel.DeltaState> newDeltaStates = new ImmutableList.Builder<>();
                            channel.recentDeltaStates().forEach(deltaState -> {
                                if (deltaState.createdAt().isAfter(cutoff)) {
                                    newDeltaStates.add(deltaState);
                                }
                            });
                            channel.recentDeltaStates().clear();
                            channel.recentDeltaStates().addAll(newDeltaStates.build());
                        }
                    }
                }
            } catch(Exception e) {
                LOG.error("Could not clean signal index tables.", e);
            } finally {
                ctx.stop();
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public void registerBeaconFrame(Dot11BeaconFrame frame) {
        if (!Strings.isNullOrEmpty(frame.ssid())) { // Don't consider broadcast frames..
            register(frame.transmitter(), frame.transmitterFingerprint(), frame.taggedParameters(), frame.ssid(), frame.meta().getChannel(), frame.meta().getSignalQuality());
        }
    }

    public void registerProbeResponseFrame(Dot11ProbeResponseFrame frame) {
        if (!Strings.isNullOrEmpty(frame.ssid())) { // Don't consider broadcast frames..
            register(frame.transmitter(), frame.transmitterFingerprint(), frame.taggedParameters(), frame.ssid(), frame.meta().getChannel(), frame.meta().getSignalQuality());
        }
    }

    private synchronized void register(String transmitter, String transmitterFingerprint, Dot11TaggedParameters taggedParameters, String ssidName, int channelNumber, int signalQuality) {
        // Ensure that the BSSID exists in the map.
        BSSID bssid;
        if (bssids.containsKey(transmitter)) {
            bssid = bssids.get(transmitter);

            // Ensure that the SSID has been recorded for this BSSID.
            if(!bssid.ssids().containsKey(ssidName)) {
                bssid.ssids().put(ssidName, SSID.create(ssidName));
            }
        } else {
            // First time we are seeing this BSSID.
            String oui = nzyme.getOUIManager().lookupBSSID(transmitter);

            if (oui == null) {
                oui = "unknown";
            }

            SSID ssid = SSID.create(ssidName);
            bssid = BSSID.create(new HashMap<String, SSID>(){{
                put(ssidName, ssid);
            }}, oui, transmitter);

            bssids.put(transmitter, bssid);
        }

        // Update 'last seen'.
        bssid.updateLastSeen();

        // Update properties that could change during the lifetime of this BSSID.
        bssid.updateIsWPS(taggedParameters.isWPS());

        // Find our SSID.
        SSID ssid = bssid.ssids().get(ssidName);
        ssid.updateSecurity(taggedParameters.getSecurityConfiguration());

        try {
            // Create or update channel.
            if (ssid.channels().containsKey(channelNumber)) {
                DateTime now = DateTime.now();
                // Update channel statistics.
                Channel channel = ssid.channels().get(channelNumber);
                channel.totalFrames().incrementAndGet();

                // Add signal quality to history of signal qualities.
                channel.recentSignalQuality().add(Channel.SignalQuality.create(now, signalQuality));

                // Update max or min quality if required.
                boolean inDelta = true;
                if (signalQuality > channel.signalMax().get()) {
                    channel.signalMax().set(signalQuality);
                    inDelta = false;
                }
                if (signalQuality < channel.signalMin().get()) {
                    channel.signalMin().set(signalQuality);
                    inDelta = false;
                }

                // Add delta state.
                channel.recentDeltaStates().add(Channel.DeltaState.create(now, inDelta));

                // Add fingerprint.
                if (transmitterFingerprint != null) {
                    channel.registerFingerprint(transmitterFingerprint);
                }

                ssid.channels().replace(channelNumber, channel);
            } else {
                // Create new channel.
                Channel channel = Channel.create(this.signalIndexManager, channelNumber, bssid.bssid(), ssid.name(), new AtomicLong(1), signalQuality, transmitterFingerprint);
                ssid.channels().put(channelNumber, channel);
            }
        } catch (NullPointerException e) {
            LOG.error(ssid);
            throw e;
        }

    }

    public Map<String, BSSID> getBSSIDs() {
        return new ImmutableMap.Builder<String, BSSID>().putAll(bssids).build();
    }

    // NOTE: This is just a list of the SSIDs and is not to be confused with SSIDs per BSSID. Multiple SSIDs are swallowed.
    public Set<String> getSSIDs() {
        Set<String> ssids = Sets.newHashSet();

        for (BSSID bssid : bssids.values()) {
            for (String ssid : bssid.ssids().keySet()) {
                if (!ssids.contains(ssid)) {
                    ssids.add(ssid);
                }
            }
        }

        return new ImmutableSet.Builder<String>().addAll(ssids).build();
    }

    public SignalDelta getSignalDelta(String bssid, String ssidName, int channelNumber) throws NoSuchNetworkException, NoSuchChannelException {
        if(!getBSSIDs().containsKey(bssid) || !getBSSIDs().get(bssid).ssids().containsKey(ssidName)) {
            throw new NoSuchNetworkException();
        }

        SSID ssid = getBSSIDs().get(bssid).ssids().get(ssidName);

        if(!ssid.channels().containsKey(channelNumber)) {
            throw new NoSuchChannelException();
        }

        return ssid.channels().get(channelNumber).expectedDelta();
    }

    public void retentionClean(int seconds) {
        try {
            for (Map.Entry<String, BSSID> entry : Lists.newArrayList(bssids.entrySet())) {
                BSSID bssid = entry.getValue();

                if (bssid.getLastSeen().isBefore(DateTime.now().minusSeconds(seconds))) {
                    LOG.debug("Retention cleaning expired BSSID [{}] from internal networks list.", bssid.bssid());
                    bssids.remove(entry.getKey());
                }
            }
        } catch(Exception e) {
            LOG.error("Error when trying to clean expired BSSIDs.", e);
        }
    }

    public static class NoSuchNetworkException extends Exception {
    }

    public static class NoSuchChannelException extends Exception {
    }
}
