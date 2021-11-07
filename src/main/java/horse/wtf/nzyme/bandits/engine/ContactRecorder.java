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

package horse.wtf.nzyme.bandits.engine;

import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.math.Stats;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.util.Tools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ContactRecorder {

    private static final Logger LOG = LogManager.getLogger(ContactRecorder.class);

    private final Object mutex = new Object();

    private final Map<UUID, Map<String, List<Integer>>> ssids;
    private final Map<UUID, Map<String, List<Integer>>> bssids;

    public ContactRecorder(int cleaningFrequencySeconds) {
        this.ssids = Maps.newHashMap();
        this.bssids = Maps.newHashMap();

        Executors.newSingleThreadScheduledExecutor(
                new ThreadFactoryBuilder()
                        .setDaemon(true)
                        .setNameFormat("contactrecorder-sync-%d")
                        .build()
        ).scheduleWithFixedDelay(() -> {
            synchronized (mutex) {
                // Write averages to database.

                // Reset recordings.
                ssids.clear();
                bssids.clear();
            }
        }, cleaningFrequencySeconds, cleaningFrequencySeconds, TimeUnit.SECONDS);
    }

    public void recordFrame(UUID contactUUID, int rssi, String bssid, Optional<String> ssid) {
        LOG.debug("Recording frame for [{}/{}] at RSSI {} and SSID {}", contactUUID, bssid, rssi, ssid);

        synchronized (mutex) {
            // BSSID
            if (!bssids.containsKey(contactUUID)) {
                bssids.put(contactUUID, Maps.newHashMap());
            }
            Map<String, List<Integer>> contactBSSID = bssids.get(contactUUID);
            if (!contactBSSID.containsKey(bssid)) {
                contactBSSID.put(bssid, Lists.newArrayList());
            }
            contactBSSID.get(bssid).add(rssi);

            // SSID
            if (ssid.isPresent() && Tools.isHumanlyReadable(ssid.get())) {
                if (!ssids.containsKey(contactUUID)) {
                    ssids.put(contactUUID, Maps.newHashMap());
                }

                Map<String, List<Integer>> contactSSID = ssids.get(contactUUID);
                if (!contactSSID.containsKey(ssid.get())) {
                    contactSSID.put(ssid.get(), Lists.newArrayList());
                }
                contactSSID.get(ssid.get()).add(rssi);
            }
        }
    }

    public static Map<UUID, Map<String, ComputationResult>> compute(Map<UUID, Map<String, List<Integer>>> population) {
        Map<UUID, Map<String, ComputationResult>> result = Maps.newHashMap();

        for (Map.Entry<UUID, Map<String, List<Integer>>> contact : population.entrySet()) {
            Map<String, ComputationResult> entryResult = Maps.newHashMap();
            for (Map.Entry<String, List<Integer>> values : contact.getValue().entrySet()) {
                Stats stats = Stats.of(values.getValue());
                entryResult.put(
                        values.getKey(),
                        ComputationResult.create(stats.mean(), stats.populationStandardDeviation())
                );
            }

            result.put(contact.getKey(), entryResult);
        }

        return result;
    }

    public Map<UUID, Map<String, List<Integer>>> getSSIDs() {
        return Maps.newHashMap(ssids);
    }

    public Map<UUID, Map<String, List<Integer>>> getBSSIDs() {
        return Maps.newHashMap(bssids);
    }

    @AutoValue
    public static abstract class ComputationResult {

        public abstract double average();
        public abstract double stdDev();

        public static ComputationResult create(double average, double stdDev) {
            return builder()
                    .average(average)
                    .stdDev(stdDev)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_ContactRecorder_ComputationResult.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder average(double average);

            public abstract Builder stdDev(double stdDev);

            public abstract ComputationResult build();
        }
    }

}
