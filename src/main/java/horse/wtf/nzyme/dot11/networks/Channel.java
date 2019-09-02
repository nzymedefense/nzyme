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
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@AutoValue
public abstract class Channel {

    public static final int RECENT_MAX_ENTRIES = 10000;

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

    @JsonIgnore
    public void registerFingerprint(String fingerprint) {
        if (!fingerprints().contains(fingerprint)) {
            fingerprints().add(fingerprint);
        }
    }

    public static Channel create(int channelNumber,
                                 String bssid,
                                 String ssid,
                                 AtomicLong totalFrames,
                                 int signal,
                                 String fingerprint) {

        List<String> fingerprints = new ArrayList<String>() {{
            if(fingerprint != null) {
                add(fingerprint);
            }
        }};

        return builder()
                .bssid(bssid)
                .ssid(ssid)
                .channelNumber(channelNumber)
                .totalFrames(totalFrames)
                .signalMin(new AtomicInteger(signal))
                .signalMax(new AtomicInteger(signal))
                .fingerprints(fingerprints)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Channel.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {

        public abstract Builder bssid(String bssid);

        public abstract Builder ssid(String ssid);

        public abstract Builder channelNumber(int channelNumber);

        public abstract Builder totalFrames(AtomicLong totalFrames);

        public abstract Builder signalMin(AtomicInteger signalMin);

        public abstract Builder signalMax(AtomicInteger signalMax);

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
