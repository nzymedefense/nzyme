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

package horse.wtf.nzyme.rest.responses.metrics;

import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TimerResponse {

    @JsonProperty("mean")
    public abstract double mean();

    @JsonProperty("max")
    public abstract double max();

    @JsonProperty("min")
    public abstract double min();

    @JsonProperty("stddev")
    public abstract double stddev();

    @JsonProperty("percentile_99")
    public abstract double percentile99();

    public static TimerResponse fromSnapshot(Snapshot s) {
        return builder()
                .mean(s.getMean())
                .max(s.getMax())
                .min(s.getMin())
                .stddev(s.getStdDev())
                .percentile99(s.get99thPercentile())
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TimerResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mean(double mean);

        public abstract Builder max(double max);

        public abstract Builder min(double min);

        public abstract Builder stddev(double stddev);

        public abstract Builder percentile99(double percentile99);

        public abstract TimerResponse build();
    }

}
