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

package horse.wtf.nzyme.dot11.networks.sigindex;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SignalInformation {

    @JsonProperty("channel")
    public abstract int channel();

    @JsonProperty("created_at")
    @Nullable
    public abstract DateTime createdAt();

    @JsonProperty("average_signal_index")
    @Nullable
    public abstract Float averageSignalIndex();

    @JsonProperty("average_signal_index_threshold")
    @Nullable
    public abstract Float averageSignalIndexThreshold();

    @JsonProperty("average_signal_quality")
    @Nullable
    public abstract Float averageSignalQuality();

    @JsonProperty("average_signal_stddev")
    @Nullable
    public abstract Float averageSignalStddev();

    @JsonProperty("average_expected_delta_lower")
    @Nullable
    public abstract Float averageExpectedDeltaLower();

    @JsonProperty("average_expected_delta_upper")
    @Nullable
    public abstract Float averageExpectedDeltaUpper();

    public static SignalInformation create(int channel, DateTime createdAt, Float averageSignalIndex, Float averageSignalIndexThreshold, Float averageSignalQuality, Float averageSignalStddev, Float averageExpectedDeltaLower, Float averageExpectedDeltaUpper) {
        return builder()
                .channel(channel)
                .createdAt(createdAt)
                .averageSignalIndex(averageSignalIndex)
                .averageSignalIndexThreshold(averageSignalIndexThreshold)
                .averageSignalQuality(averageSignalQuality)
                .averageSignalStddev(averageSignalStddev)
                .averageExpectedDeltaLower(averageExpectedDeltaLower)
                .averageExpectedDeltaUpper(averageExpectedDeltaUpper)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalInformation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder channel(int channel);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder averageSignalIndex(Float averageSignalIndex);

        public abstract Builder averageSignalIndexThreshold(Float averageSignalIndexThreshold);

        public abstract Builder averageSignalQuality(Float averageSignalQuality);

        public abstract Builder averageSignalStddev(Float averageSignalStddev);

        public abstract Builder averageExpectedDeltaLower(Float averageExpectedDeltaLower);

        public abstract Builder averageExpectedDeltaUpper(Float averageExpectedDeltaUpper);

        public abstract SignalInformation build();
    }

}
