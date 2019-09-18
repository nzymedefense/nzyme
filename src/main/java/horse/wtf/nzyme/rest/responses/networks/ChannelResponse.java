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

package horse.wtf.nzyme.rest.responses.networks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@AutoValue
public abstract class ChannelResponse {

    @JsonProperty("channel_number")
    public abstract int channelNumber();

    @JsonProperty("bssid")
    public abstract String bssid();

    @JsonProperty("ssid")
    public abstract String ssid();

    @JsonProperty("total_frames")
    public abstract long totalFrames();

    @JsonProperty("fingerprints")
    public abstract List<String> fingerprints();

    @JsonProperty("signal_index_distribution")
    public abstract Map<Integer, AtomicLong> signalIndexDistribution();

    @JsonProperty("signal_index_history")
    @Nullable
    public abstract List<List<Long>> signalIndexHistory();

    public static ChannelResponse create(int channelNumber, String bssid, String ssid, long totalFrames, List<String> fingerprints, Map<Integer, AtomicLong> signalIndexDistribution, List<List<Long>> signalIndexHistory) {
        return builder()
                .channelNumber(channelNumber)
                .bssid(bssid)
                .ssid(ssid)
                .totalFrames(totalFrames)
                .fingerprints(fingerprints)
                .signalIndexDistribution(signalIndexDistribution)
                .signalIndexHistory(signalIndexHistory)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ChannelResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder channelNumber(int channelNumber);

        public abstract Builder bssid(String bssid);

        public abstract Builder ssid(String ssid);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder signalIndexDistribution(Map<Integer, AtomicLong> signalIndexDistribution);

        public abstract Builder signalIndexHistory(List<List<Long>> signalIndexHistory);

        public abstract ChannelResponse build();
    }

}
