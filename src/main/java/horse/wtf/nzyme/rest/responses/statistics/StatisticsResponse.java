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

package horse.wtf.nzyme.rest.responses.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoValue
public abstract class StatisticsResponse {

    @JsonProperty("total_frames")
    public abstract Long totalFrames();

    @JsonProperty("malformed_frames")
    public abstract Long malformedFrames();

    @JsonProperty("frame_types")
    public abstract Map<String, Long> frameTypes();

    @JsonProperty
    public abstract Map<Integer, ChannelStatisticsResponse> channels();

    @JsonProperty("current_probing_devices")
    public abstract Set<String> currentProbingDevices();

    @JsonProperty("current_ssids")
    public abstract Set<String> currentSSIDs();

    @JsonProperty("current_bssids")
    public abstract Set<String> currentBSSIDs();

    @JsonProperty("histogram_probing_devices")
    public abstract Map<String, Long> histogramProbingDevices();

    @JsonProperty("histogram_bssids")
    public abstract Map<String, Long> histogramBSSIDs();

    public static StatisticsResponse create(Long totalFrames, Long malformedFrames, Map<String, Long> frameTypes, Map<Integer, ChannelStatisticsResponse> channels, Set<String> currentProbingDevices, Set<String> currentSSIDs, Set<String> currentBSSIDs, Map<String, Long> histogramProbingDevices, Map<String, Long> histogramBSSIDs) {
        return builder()
                .totalFrames(totalFrames)
                .malformedFrames(malformedFrames)
                .frameTypes(frameTypes)
                .channels(channels)
                .currentProbingDevices(currentProbingDevices)
                .currentSSIDs(currentSSIDs)
                .currentBSSIDs(currentBSSIDs)
                .histogramProbingDevices(histogramProbingDevices)
                .histogramBSSIDs(histogramBSSIDs)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_StatisticsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder totalFrames(Long totalFrames);

        public abstract Builder malformedFrames(Long malformedFrames);

        public abstract Builder frameTypes(Map<String, Long> frameTypes);

        public abstract Builder channels(Map<Integer, ChannelStatisticsResponse> channels);

        public abstract Builder currentProbingDevices(Set<String> currentProbingDevices);

        public abstract Builder currentSSIDs(Set<String> currentSSIDs);

        public abstract Builder currentBSSIDs(Set<String> currentBSSIDs);

        public abstract Builder histogramProbingDevices(Map<String, Long> histogramProbingDevices);

        public abstract Builder histogramBSSIDs(Map<String, Long> histogramBSSIDs);

        public abstract StatisticsResponse build();
    }

}
