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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.math.Stats;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@AutoValue
public abstract class Channel {

    public static final int RECENT_MAX_ENTRIES = 500;

    @JsonProperty("total_frames")
    public abstract AtomicLong totalFrames();

    @JsonProperty("signal_quality_min")
    public abstract AtomicInteger signalMin();

    @JsonProperty("signal_quality_max")
    public abstract AtomicInteger signalMax();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    @JsonIgnore
    public abstract EvictingQueue<Integer> recentSignalQuality();

    @JsonIgnore
    public abstract EvictingQueue<Boolean> recentDeltaStates();

    @JsonIgnore
    public void registerFingerprint(String fingerprint) {
        if (!fingerprints().contains(fingerprint)) {
            fingerprints().add(fingerprint);
        }
    }

    @JsonProperty("signal_quality_avg_recent")
    public int signalQualityRecentAverage() {
        if(recentSignalQuality().isEmpty()) {
            return 0;
        }

        return Long.valueOf(Math.round(Stats.of(recentSignalQuality()).mean())).intValue();
    }

    @JsonProperty("signal_index")
    public float signalIndex() {
        if (recentDeltaStates().isEmpty()) {
            return 0;
        }

        long out = 0;

        for (Boolean inDelta : recentDeltaStates()) {
            if(!inDelta) {
                out += 1;
            }
        }

        return ((float)out)/(float)recentDeltaStates().size()*100;
    }

    @JsonProperty("signal_quality_stddev_recent")
    public double signalQualityRecentStddev() {
        if(recentSignalQuality().isEmpty()) {
            return 0.0;
        }

        return Stats.of(recentSignalQuality()).populationStandardDeviation();
    }

    @JsonProperty("expected_delta")
    public SignalDelta expectedDelta() {
        int delta = Long.valueOf(Math.round(Math.pow(signalQualityRecentStddev(), 2)/3)).intValue();
        int lower = signalQualityRecentAverage()-delta;
        int upper = signalQualityRecentAverage()+delta;

        return SignalDelta.create(
                lower,
                upper
        );
    }

    public static Channel create(AtomicLong totalFrames, int signal, String fingerprint) {
        EvictingQueue<Integer> q = EvictingQueue.create(RECENT_MAX_ENTRIES);
        EvictingQueue<Boolean> d = EvictingQueue.create(RECENT_MAX_ENTRIES);

        List<String> fingerprints = new ArrayList<String>() {{
            if(fingerprint != null) {
                add(fingerprint);
            }
        }};

        return builder()
                .totalFrames(totalFrames)
                .signalMin(new AtomicInteger(signal))
                .signalMax(new AtomicInteger(signal))
                .recentSignalQuality(q)
                .recentDeltaStates(d)
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Channel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder totalFrames(AtomicLong totalFrames);

        public abstract Builder signalMin(AtomicInteger signalMin);

        public abstract Builder signalMax(AtomicInteger signalMax);

        public abstract Builder recentSignalQuality(EvictingQueue<Integer> recentSignalQuality);

        public abstract Builder recentDeltaStates(EvictingQueue<Boolean> recentDeltaStates);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Channel build();
    }

}
