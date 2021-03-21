/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
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
