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
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.Dot11TaggedParameters;
import horse.wtf.nzyme.dot11.frames.*;
import horse.wtf.nzyme.dot11.networks.beaconrate.BeaconRateManager;
import horse.wtf.nzyme.dot11.networks.signalstrength.SignalStrengthTable;
import horse.wtf.nzyme.util.MetricNames;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Networks {

    private static final Logger LOG = LogManager.getLogger(Networks.class);

    private final Map<String, BSSID> bssids;

    private final Nzyme nzyme;

    private final BeaconRateManager beaconRateManager;

    public Networks(Nzyme nzyme) {
        this.nzyme = nzyme;
        this.bssids = Maps.newConcurrentMap();
        this.beaconRateManager = new BeaconRateManager(nzyme);

        // Metric: Combined length of all signal strength tables.
        if (!nzyme.getMetrics().getGauges().containsKey(MetricNames.NETWORKS_SIGNAL_STRENGTH_MEASUREMENTS)) {
            nzyme.getMetrics().register(MetricNames.NETWORKS_SIGNAL_STRENGTH_MEASUREMENTS, (Gauge<Long>) () -> {
                long result = 0;
                for (BSSID bssid : bssids.values()) {
                    for (SSID ssid : bssid.ssids().values()) {
                        for (Channel channel : ssid.channels().values()) {
                            result += channel.signalStrengthTable().getSize();
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

        // Regularly delete old entries in signal strength tables.
        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("signalstrengths-cleaner")
                        .build()
        ).scheduleAtFixedRate(() -> {
            for (BSSID bssid : bssids.values()) {
                for (SSID ssid : bssid.ssids().values()) {
                    for (Channel channel : ssid.channels().values()) {
                        channel.signalStrengthTable().retentionClean(300); // TODO ZSCORE make configurable
                    }
                }
            }
        }, 10, 10, TimeUnit.SECONDS); // TODO ZSCORE make configurable
    }

    public void registerBeaconFrame(Dot11BeaconFrame frame) {
        if (!Strings.isNullOrEmpty(frame.ssid())) { // Don't consider broadcast frames..
            register(Dot11FrameSubtype.BEACON, frame.transmitter(), frame.transmitterFingerprint(), frame.taggedParameters(), frame.ssid(), frame.meta().getChannel(), frame.meta().getSignalQuality());
        }
    }

    public void registerProbeResponseFrame(Dot11ProbeResponseFrame frame) {
        if (!Strings.isNullOrEmpty(frame.ssid())) { // Don't consider broadcast frames..
            register(Dot11FrameSubtype.PROBE_RESPONSE, frame.transmitter(), frame.transmitterFingerprint(), frame.taggedParameters(), frame.ssid(), frame.meta().getChannel(), frame.meta().getSignalQuality());
        }
    }

    private void register(byte subtype,
                          String transmitter,
                          String transmitterFingerprint,
                          Dot11TaggedParameters taggedParameters,
                          String ssidName,
                          int channelNumber,
                          int signalQuality) {
        // Ensure that the BSSID exists in the map.
        BSSID bssid;
        if (bssids.containsKey(transmitter)) {
            bssid = bssids.get(transmitter);

            // Ensure that the SSID has been recorded for this BSSID.
            if (!bssid.ssids().containsKey(ssidName)) {
                bssid.ssids().put(ssidName, SSID.create(ssidName, bssid.bssid(), beaconRateManager));
            }
        } else {
            // First time we are seeing this BSSID.
            String oui = nzyme.getOUIManager().lookupBSSID(transmitter);

            if (oui == null) {
                oui = "unknown";
            }

            SSID ssid = SSID.create(ssidName, transmitter, beaconRateManager);
            bssid = BSSID.create(new HashMap<String, SSID>() {{
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

        // Update beacon counter.
        if (subtype == Dot11FrameSubtype.BEACON) {
            // Used for beacon rate calculation.
            ssid.beaconCount.incrementAndGet();
        }

        DateTime now = DateTime.now();
        try {
            // Create or update channel.
            if (ssid.channels().containsKey(channelNumber)) {
                // Update channel statistics.
                Channel channel = ssid.channels().get(channelNumber);
                channel.totalFrames().incrementAndGet();

                // Add fingerprint.
                if (transmitterFingerprint != null) {
                    channel.registerFingerprint(transmitterFingerprint);
                }

                // Record signal strength.
                channel.signalStrengthTable().recordSignalStrength(
                        SignalStrengthTable.SignalStrength.create(
                                now,
                                signalQuality,
                                channel.signalStrengthTable().calculateZScore(signalQuality)
                        )
                );

                ssid.channels().replace(channelNumber, channel);
            } else {
                // Create new channel.
                Channel channel = Channel.create(
                        nzyme,
                        channelNumber,
                        bssid.bssid(),
                        ssid.name(),
                        new AtomicLong(1),
                        transmitterFingerprint
                );

                // Record signal strength.
                channel.signalStrengthTable().recordSignalStrength(
                        SignalStrengthTable.SignalStrength.create(now, signalQuality, 0.0)
                );

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

    @Nullable
    public Channel findChannel(String bssidMac, String ssidName, int channelNumber) {
        for (BSSID bssid : bssids.values()) {
            if (!bssid.bssid().equals(bssidMac)) {
                continue;
            }

            for (SSID ssid : bssid.ssids().values()) {
                if (!ssid.name().equals(ssidName)) {
                    continue;
                }

                for (Channel channel : ssid.channels().values()) {
                    if (channel.channelNumber() == channelNumber) {
                        return channel;
                    }
                }
            }
        }

        return null;
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
