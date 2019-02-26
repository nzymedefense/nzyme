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

import com.codahale.metrics.Meter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class MeterResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("avg_1")
    public abstract double avg1();

    @JsonProperty("avg_5")
    public abstract double avg5();

    @JsonProperty("avg_15")
    public abstract double avg15();

    @JsonProperty("mean")
    public abstract double mean();

    public static MeterResponse fromMeter(Meter meter) {
        return builder()
                .count(meter.getCount())
                .avg1(meter.getOneMinuteRate())
                .avg5(meter.getFiveMinuteRate())
                .avg15(meter.getFifteenMinuteRate())
                .mean(meter.getMeanRate())
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MeterResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder avg1(double avg1);

        public abstract Builder avg5(double avg5);

        public abstract Builder avg15(double avg15);

        public abstract Builder mean(double mean);

        public abstract MeterResponse build();
    }

}
