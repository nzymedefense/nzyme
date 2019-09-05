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
import com.codahale.metrics.Timer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.concurrent.TimeUnit;

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

    @JsonProperty
    public abstract long count();

    public static TimerResponse fromTimer(Timer t) {
        Snapshot s = t.getSnapshot();

        return builder()
                .mean(TimeUnit.MICROSECONDS.convert((long) s.getMean(), TimeUnit.NANOSECONDS))
                .max(TimeUnit.MICROSECONDS.convert(s.getMax(), TimeUnit.NANOSECONDS))
                .min(TimeUnit.MICROSECONDS.convert(s.getMin(), TimeUnit.NANOSECONDS))
                .stddev(TimeUnit.MICROSECONDS.convert((long) s.getStdDev(), TimeUnit.NANOSECONDS))
                .percentile99(TimeUnit.MICROSECONDS.convert((long) s.get99thPercentile(), TimeUnit.NANOSECONDS))
                .count(t.getCount())
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

        public abstract Builder count(long count);

        public abstract TimerResponse build();
    }

}
