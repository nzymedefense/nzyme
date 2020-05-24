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

package horse.wtf.nzyme.bandits.trackers.trackerlogic;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.NzymeTracker;
import horse.wtf.nzyme.bandits.BanditHashCalculator;
import horse.wtf.nzyme.bandits.trackers.TrackerState;
import horse.wtf.nzyme.bandits.trackers.protobuf.TrackerMessage;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class TrackerStateWatchdog {

    private final NzymeTracker nzyme;

    private final AtomicReference<List<TrackerState>> states;

    private final AtomicReference<Optional<DateTime>> lastPingReceived;
    private final AtomicReference<Optional<Integer>> lastRSSIReceived;
    private final AtomicReference<Optional<String>> lastBanditHashReceived;

    public TrackerStateWatchdog(NzymeTracker nzyme) {
        this.nzyme = nzyme;

        this.states = new AtomicReference<>(new ArrayList<>(){{
            add(TrackerState.DARK);
            add(TrackerState.OUT_OF_SYNC);
        }});

        this.lastPingReceived = new AtomicReference<>(Optional.empty());
        this.lastRSSIReceived = new AtomicReference<>(Optional.empty());
        this.lastBanditHashReceived = new AtomicReference<>(Optional.empty());
    }

    public void initialize() {
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("trackerstate-watchdog-%d")
                .build())
                .scheduleAtFixedRate(() -> {
                    List<TrackerState> previousState = Lists.newArrayList(states.get());
                    List<TrackerState> result = Lists.newArrayList();

                    if (lastPingReceived.get().isEmpty()) {
                        // Never received a ping at all.
                        result.add(TrackerState.DARK);
                    } else {
                        // Are we still connected?
                        lastPingReceived.get().ifPresent(time -> {
                            if (time.isAfter(DateTime.now().minusSeconds(20))) {
                                result.add(TrackerState.ONLINE);

                                // Is the signal weak?
                                lastRSSIReceived.get().ifPresent(rssi -> {
                                    if (rssi < 50) { // TODO find a good threshold
                                        result.add(TrackerState.WEAK);
                                    }
                                });

                                // Are we out of sync?
                                lastBanditHashReceived.get().ifPresent(hash -> {
                                    String ourHash = BanditHashCalculator.calculate(nzyme.getBanditManager().getBanditList());
                                    if (!ourHash.equals(hash)) {
                                        result.add(TrackerState.OUT_OF_SYNC);
                                    }
                                });
                            } else {
                                // Last ping was too long ago.
                                result.add(TrackerState.DARK);
                            }
                        });
                    }

                    states.set(result);

                    Collections.sort(result);
                    Collections.sort(previousState);
                    if (!result.equals(previousState)) {
                        stateChanged();
                    }
                }, 5, 10, TimeUnit.SECONDS);
    }

    public void registerPing(TrackerMessage.Ping ping, int rssi) {
        if (ping.getNodeType() != TrackerMessage.Ping.NodeType.LEADER) {
            return;
        }

        this.lastPingReceived.set(Optional.of(new DateTime(ping.getTimestamp())));
        this.lastRSSIReceived.set(Optional.of(rssi));
        this.lastBanditHashReceived.set(Optional.of(ping.getBanditHash()));
    }

    private void stateChanged() {
        nzyme.getGroundStation().handleTrackerConnectionStateChange(states.get());
    }

}