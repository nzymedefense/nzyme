package horse.wtf.nzyme.statistics;

import com.google.common.collect.Maps;
import horse.wtf.nzyme.Nzyme;
import horse.wtf.nzyme.graylog.GraylogFieldNames;
import horse.wtf.nzyme.graylog.Notification;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {

    private final Nzyme nzyme;

    private final AtomicLong frameCount;
    private final AtomicLong malformedCount;
    private final Map<String, AtomicLong> frameTypes;

    private final Map<Integer, AtomicLong> channelCounts;
    private final Map<Integer, AtomicLong> channelMalformedCounts;

    // Remember to reset these in resetStats()
    private final Map<String, AtomicLong> probingDevices;
    private final Map<String, AtomicLong> accessPoints;
    private final Map<String, AtomicLong> beaconedNetworks;

    public Statistics(Nzyme nzyme) {
        this.nzyme = nzyme;

        this.frameCount = new AtomicLong(0);
        this.malformedCount = new AtomicLong(0);

        this.channelCounts = Maps.newHashMap();
        this.channelMalformedCounts = Maps.newHashMap();

        this.frameTypes = Maps.newHashMap();
        this.probingDevices = Maps.newHashMap();
        this.accessPoints = Maps.newHashMap();
        this.beaconedNetworks = Maps.newHashMap();
    }

    public void resetAccumulativeTicks() {
        probingDevices.clear();
        accessPoints.clear();
        beaconedNetworks.clear();
    }

    public void tickFrameCount(int channel) {
        frameCount.incrementAndGet();
        tickInMap(channel, channelCounts);
    }

    public void tickMalformedCountAndNotify(int channel) {
        this.nzyme.notify(
                new Notification("Malformed frame received.", nzyme.getChannelHopper().getCurrentChannel())
                        .addField(GraylogFieldNames.SUBTYPE, "malformed"), null);

        malformedCount.incrementAndGet();
        tickInMap(channel, channelMalformedCounts);
    }

    public void tickType(String type) {
        tickInMap(type, frameTypes);
    }

    public void tickProbingDevice(String bssid) {
        tickInMap(bssid, probingDevices);
    }

    public void tickAccessPoint(String bssid) {
        tickInMap(bssid, accessPoints);
    }

    public void tickBeaconedNetwork(String ssid) {
        tickInMap(ssid, beaconedNetworks);
    }

    public Map<String, AtomicLong> getProbingDevices() {
        return probingDevices;
    }

    public Map<String, AtomicLong> getAccessPoints() {
        return accessPoints;
    }

    public Map<String, AtomicLong> getBeaconedNetworks() {
        return beaconedNetworks;
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
