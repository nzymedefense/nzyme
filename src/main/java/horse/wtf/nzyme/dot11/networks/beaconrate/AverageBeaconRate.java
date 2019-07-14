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

package horse.wtf.nzyme.dot11.networks.beaconrate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class AverageBeaconRate {

    @JsonProperty("rate")
    public abstract float rate();

    @JsonProperty("channel")
    public abstract int channel();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static AverageBeaconRate create(float rate, int channel, DateTime createdAt) {
        return builder()
                .rate(rate)
                .channel(channel)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AverageBeaconRate.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder rate(float rate);

        public abstract Builder channel(int channel);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract AverageBeaconRate build();
    }

}
