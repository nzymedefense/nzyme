package horse.wtf.nzyme;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class Statistics {

    private final AtomicLong frameCount;
    private final AtomicLong malformedCount;
    private final Map<String, AtomicLong> types;

    public Statistics() {
        this.frameCount = new AtomicLong(0);
        this.malformedCount = new AtomicLong(0);
        this.types = Maps.newHashMap();
    }

    public void tickFrameCount() {
        frameCount.incrementAndGet();
    }

    public void tickMalformedCount() { malformedCount.incrementAndGet(); }

    public void tickType(String type) {
        if(types.containsKey(type)) {
            types.get(type).incrementAndGet();
        } else {
            types.put(type, new AtomicLong(1));
        }
    }

    public AtomicLong getFrameCount() {
        return frameCount;
    }

    public AtomicLong getMalformedCount() {
        return malformedCount;
    }

    public Map<String, AtomicLong> getTypes() {
        return types;
    }

}
