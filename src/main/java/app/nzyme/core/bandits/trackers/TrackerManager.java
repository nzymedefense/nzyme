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

package app.nzyme.core.bandits.trackers;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import app.nzyme.core.bandits.trackers.protobuf.TrackerMessage;
import app.nzyme.core.bandits.trackers.trackerlogic.TrackerStateWatchdog;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TrackerManager {

    public static final int DARK_TIMEOUT_SECONDS = 30;
    private static final int RETENTION_MINUTES = 60;

    private final AtomicReference<Map<String, Tracker>> activeTrackers;

    public TrackerManager() {
        activeTrackers = new AtomicReference<>(Maps.newHashMap());

        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("tracker-manager-cleaner-%d")
                .build())
                .scheduleAtFixedRate(() -> {
                    Map<String, Tracker> copy = new HashMap<>(activeTrackers.get());
                    Map<String, Tracker> result = Maps.newHashMap();

                    for (Map.Entry<String, Tracker> tracker : copy.entrySet()) {
                        if (tracker.getValue().getLastSeen().isAfter(DateTime.now().minusMinutes(RETENTION_MINUTES))) {
                            result.put(tracker.getKey(), tracker.getValue());
                        }
                    }

                    activeTrackers.set(result);
                }, RETENTION_MINUTES, 1, TimeUnit.MINUTES);
    }

    public void registerTrackerPing(TrackerMessage.Ping ping, int rssi) {
        if (!activeTrackers.get().containsKey(ping.getSource())) {
            // Register new tracker.
            activeTrackers.get().put(
                    ping.getSource(),
                    new Tracker(
                            ping.getSource(),
                            DateTime.now(),
                            ping.getVersion(),
                            ping.getTrackingMode(),
                            rssi
                    )
            );

        } else {
            // Update existing tracker.
            Tracker tracker = activeTrackers.get().get(ping.getSource());
            tracker.setLastSeen(DateTime.now());
            tracker.setVersion(ping.getVersion());
            tracker.setTrackingMode(ping.getTrackingMode());
            tracker.setRssi(rssi);
        }
    }

    public Map<String, Tracker> getTrackers() {
        return new HashMap<>(activeTrackers.get());
    }

    public static TrackerState decideTrackerState(Tracker tracker) {
        if (tracker.getLastSeen().isBefore(DateTime.now().minusSeconds(TrackerManager.DARK_TIMEOUT_SECONDS))) {
            return TrackerState.DARK;
        } else {
            if (tracker.getRssi() < TrackerStateWatchdog.WEAK_RSSI_LIMIT) {
                return TrackerState.WEAK;
            } else {
                return TrackerState.ONLINE;
            }
        }
    }

}
