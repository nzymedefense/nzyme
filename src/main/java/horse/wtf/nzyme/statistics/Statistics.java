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

package horse.wtf.nzyme.statistics;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import horse.wtf.nzyme.UplinkHandler;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;
import horse.wtf.nzyme.notifications.FieldNames;
import horse.wtf.nzyme.notifications.Notification;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {

    private final UplinkHandler uplink;

    private final AtomicLong frameCount;
    private final AtomicLong recentFrameCount;
    private final AtomicLong recentFrameCountTemp;
    private final AtomicLong malformedCount;
    private final Map<String, AtomicLong> frameTypes;

    private final Map<Integer, AtomicLong> channelCounts;
    private final Map<Integer, AtomicLong> channelMalformedCounts;

    // Remember to reset these in resetStats()

    public Statistics(UplinkHandler uplink) {
        this.uplink = uplink;

        this.frameCount = new AtomicLong(0);
        this.recentFrameCount = new AtomicLong(0);
        this.recentFrameCountTemp = new AtomicLong(0);
        this.malformedCount = new AtomicLong(0);

        this.channelCounts = Maps.newHashMap();
        this.channelMalformedCounts = Maps.newHashMap();

        this.frameTypes = Maps.newHashMap();

        // Periodically clean up recent statistics.
        Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("statistics-recent-cleaner-%d")
                .build())
                .scheduleAtFixedRate(this::resetRecentFrameCount, 1, 1, TimeUnit.MINUTES);
    }

    public void tickFrameCount(Dot11MetaInformation meta) {
        frameCount.incrementAndGet();
        recentFrameCountTemp.incrementAndGet();
        tickInMap(meta.getChannel(), channelCounts);
    }

    public void resetRecentFrameCount() {
        recentFrameCount.set(recentFrameCountTemp.get());
        recentFrameCountTemp.set(0);
    }

    public long getRecentFrameCount() {
        return recentFrameCount.get();
    }

    public void tickMalformedCountAndNotify(Dot11MetaInformation meta) {
        int channel = 0;
        if(meta != null) {
            channel = meta.getChannel();
            frameCount.incrementAndGet();
            tickInMap(meta.getChannel(), channelCounts);
        }

        uplink.notifyUplinks(
                new Notification("Malformed frame received.", channel)
                        .addField(FieldNames.SUBTYPE, "malformed"), meta);

        malformedCount.incrementAndGet();

        tickInMap(channel, channelMalformedCounts);
    }

    public void tickType(String type) {
        tickInMap(type, frameTypes);
    }

    public AtomicLong getFrameCount() {
        return frameCount;
    }

    public AtomicLong getMalformedCount() {
        return malformedCount;
    }

    public Map<String, AtomicLong> getFrameTypes() {
        return frameTypes;
    }

    public Map<Integer, AtomicLong> getChannelCounts() {
        return channelCounts;
    }

    public Map<Integer, AtomicLong> getChannelMalformedCounts() {
        return channelMalformedCounts;
    }

    private void tickInMap(String key, Map<String, AtomicLong> map) {
        if(map.containsKey(key)) {
            map.get(key).incrementAndGet();
        } else {
            map.put(key, new AtomicLong(1));
        }
    }

    private void tickInMap(int key, Map<Integer, AtomicLong> map) {
        if(map.containsKey(key)) {
            map.get(key).incrementAndGet();
        } else {
            map.put(key, new AtomicLong(1));
        }
    }

}
