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

package horse.wtf.nzyme.bandits.trackers;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TrackerManager {

    private static final Logger LOG = LogManager.getLogger(TrackerManager.class);

    public static final int DARK_TIMEOUT_SECONDS = 15;
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
        // Check for time drift.
        long drift = new DateTime(ping.getTimestamp()).minus(DateTime.now().getMillis()).getMillis();
        if (drift > 5000 || drift < -5000) {
            LOG.warn("Tracker [{}] has a significant time drift of <{}ms>. " +
                    "Make sure that all nzyme components have their clocks synchronized using NTP.", ping.getSource(), drift);
        }

        if (!activeTrackers.get().containsKey(ping.getSource())) {
            // Register new tracker.
            activeTrackers.get().put(
                    ping.getSource(),
                    new Tracker(
                            ping.getSource(),
                            DateTime.now(),
                            ping.getVersion(),
                            ping.getBanditHash(),
                            ping.getBanditCount(),
                            drift,
                            ping.getTrackingMode(),
                            rssi
                    )
            );

        } else {
            // Update existing tracker.
            Tracker tracker = activeTrackers.get().get(ping.getSource());
            tracker.setLastSeen(DateTime.now());
            tracker.setVersion(ping.getVersion());
            tracker.setBanditHash(ping.getBanditHash());
            tracker.setBanditCount(ping.getBanditCount());
            tracker.setTrackingMode(ping.getTrackingMode());
            tracker.setRssi(rssi);

            tracker.setDrift(drift);
        }
    }

    public Map<String, Tracker> getTrackers() {
        return new HashMap<>(activeTrackers.get());
    }

}
