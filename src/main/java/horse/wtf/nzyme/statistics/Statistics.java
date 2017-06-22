package horse.wtf.nzyme.statistics;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {

    private final AtomicLong frameCount;
    private final AtomicLong malformedCount;
    private final Map<String, AtomicLong> frameTypes;

    // Remember to reset these in resetStats()
    private final Map<String, AtomicLong> probingDevices;
    private final Map<String, AtomicLong> accessPoints;
    private final Map<String, AtomicLong> beaconedNetworks;

    public Statistics() {
        this.frameCount = new AtomicLong(0);
        this.malformedCount = new AtomicLong(0);

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

    public void tickFrameCount() {
        frameCount.incrementAndGet();
    }

    public void tickMalformedCount() { malformedCount.incrementAndGet(); }

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

    private void tickInMap(String key, Map<String, AtomicLong> map) {
        if(map.containsKey(key)) {
            map.get(key).incrementAndGet();
        } else {
            map.put(key, new AtomicLong(1));
        }
    }

}
