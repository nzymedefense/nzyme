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
import com.google.common.collect.ImmutableList;
import com.google.common.math.Stats;
import horse.wtf.nzyme.dot11.networks.sigindex.AverageSignalIndex;
import horse.wtf.nzyme.dot11.networks.sigindex.SignalIndexManager;
import horse.wtf.nzyme.dot11.networks.sigindex.SignalInformation;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@AutoValue
public abstract class Channel {

    public static final int RECENT_MAX_ENTRIES = 10000;

    @JsonIgnore
    public abstract SignalIndexManager signalIndexManager();

    @JsonProperty("channel_number")
    public abstract int channelNumber();

    @JsonProperty("bssid")
    public abstract String bssid();

    @JsonProperty("ssid")
    public abstract String ssid();

    @JsonProperty("total_frames")
    public abstract AtomicLong totalFrames();

    @JsonProperty("signal_quality_min")
    public abstract AtomicInteger signalMin();

    @JsonProperty("signal_quality_max")
    public abstract AtomicInteger signalMax();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    @JsonProperty("signal_history")
    public List<SignalInformation> signalHistory = Collections.emptyList();

    @JsonIgnore
    public abstract EvictingQueue<SignalQuality> recentSignalQuality();

    @JsonIgnore
    public abstract EvictingQueue<DeltaState> recentDeltaStates();

    @JsonIgnore
    public List<Integer> getRecentSignalQualityValues() {
        ImmutableList.Builder<Integer> values = new ImmutableList.Builder<>();

        for (SignalQuality x : recentSignalQuality()) {
            values.add(x.quality());
        }


        return values.build();
    }

    @JsonIgnore
    public List<Boolean> getRecentDeltaStateValues() {
        ImmutableList.Builder<Boolean> values = new ImmutableList.Builder<>();

        for (DeltaState x : recentDeltaStates()) {
            values.add(x.state());
        }


        return values.build();
    }

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

        return Long.valueOf(Math.round(Stats.of(getRecentSignalQualityValues()).mean())).intValue();
    }

    @JsonProperty("signal_index")
    public float signalIndex() {
        if (recentDeltaStates().isEmpty()) {
            return 0;
        }

        long out = 0;

        for (Boolean inDelta : getRecentDeltaStateValues()) {
            if(!inDelta) {
                out += 1;
            }
        }

        return ((float)out)/(float)recentDeltaStates().size()*100;
    }

    @JsonProperty("signal_index_threshold")
    public AverageSignalIndex signalIndexThreshold() {
        return signalIndexManager().getRecentAverageSignalIndex(bssid(), ssid(), channelNumber(), recentDeltaStates().size());
    }

    public enum SignalIndexStatus {
        TRAINING, TEMP_NA, OK, ANOMALY
    }

    @JsonProperty("signal_index_status")
    public SignalIndexStatus signalIndexStatus() {
        AverageSignalIndex avg = signalIndexThreshold();

        if (avg.inTraining()) {
            return SignalIndexStatus.TRAINING;
        }

        if (!avg.hadEnoughData()) {
            return SignalIndexStatus.TEMP_NA;
        }

        if (signalIndex() > avg.index()) {
            return SignalIndexStatus.ANOMALY;
        } else {
            return SignalIndexStatus.OK;
        }
    }

    @JsonProperty("signal_quality_stddev_recent")
    public double signalQualityRecentStddev() {
        if(recentSignalQuality().isEmpty()) {
            return 0.0;
        }

        return Stats.of(getRecentSignalQualityValues()).populationStandardDeviation();
    }

    @JsonProperty("expected_delta")
    public SignalDelta expectedDelta() {
        int delta = Long.valueOf(Math.round(Math.pow(signalQualityRecentStddev(), 2)/3)).intValue(); // TODO make factor configurable
        int lower = signalQualityRecentAverage()-delta;
        int upper = signalQualityRecentAverage()+delta;

        return SignalDelta.create(
                lower,
                upper
        );
    }

    @JsonIgnore
    public void setSignalHistory(List<SignalInformation> history) {
        this.signalHistory = history;
    }

    public static Channel create(SignalIndexManager signalIndexManager, int channelNumber, String bssid, String ssid, AtomicLong totalFrames, int signal, String fingerprint) {
        EvictingQueue<SignalQuality> q = EvictingQueue.create(RECENT_MAX_ENTRIES);
        EvictingQueue<DeltaState> d = EvictingQueue.create(RECENT_MAX_ENTRIES);

        List<String> fingerprints = new ArrayList<String>() {{
            if(fingerprint != null) {
                add(fingerprint);
            }
        }};

        return builder()
                .signalIndexManager(signalIndexManager)
                .bssid(bssid)
                .ssid(ssid)
                .channelNumber(channelNumber)
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
        public abstract Builder signalIndexManager(SignalIndexManager signalIndexManager);

        public abstract Builder bssid(String bssid);

        public abstract Builder ssid(String ssid);

        public abstract Builder channelNumber(int channelNumber);

        public abstract Builder totalFrames(AtomicLong totalFrames);

        public abstract Builder signalMin(AtomicInteger signalMin);

        public abstract Builder signalMax(AtomicInteger signalMax);

        public abstract Builder recentSignalQuality(EvictingQueue<SignalQuality> recentSignalQuality);

        public abstract Builder recentDeltaStates(EvictingQueue<DeltaState> recentDeltaStates);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Channel build();
    }

    @AutoValue
    static abstract class SignalQuality {

        public abstract DateTime createdAt();

        public abstract Integer quality();

        public static SignalQuality create(DateTime createdAt, Integer quality) {
            return builder()
                    .createdAt(createdAt)
                    .quality(quality)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_Channel_SignalQuality.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder createdAt(DateTime createdAt);

            public abstract Builder quality(Integer quality);

            public abstract SignalQuality build();
        }
    }

    @AutoValue
    static abstract class DeltaState {

        public abstract DateTime createdAt();

        public abstract Boolean state();

        public static DeltaState create(DateTime createdAt, Boolean state) {
            return builder()
                    .createdAt(createdAt)
                    .state(state)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_Channel_DeltaState.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder createdAt(DateTime createdAt);

            public abstract Builder state(Boolean state);

            public abstract DeltaState build();
        }
    }

}
