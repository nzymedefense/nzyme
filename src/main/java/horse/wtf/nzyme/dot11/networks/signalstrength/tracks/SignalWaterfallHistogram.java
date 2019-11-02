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

package horse.wtf.nzyme.dot11.networks.signalstrength.tracks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SignalWaterfallHistogram {

    @JsonProperty("z")
    public abstract List z();

    @JsonProperty("x")
    public abstract List x();

    @JsonProperty("y")
    public abstract List y();

    public static SignalWaterfallHistogram create(List z, List x, List y) {
        return builder()
                .z(z)
                .x(x)
                .y(y)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalWaterfallHistogram.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder z(List z);

        public abstract Builder x(List x);

        public abstract Builder y(List y);

        public abstract SignalWaterfallHistogram build();
    }

}
