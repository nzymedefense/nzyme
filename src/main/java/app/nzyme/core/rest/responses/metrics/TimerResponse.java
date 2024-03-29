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

package app.nzyme.core.rest.responses.metrics;

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

    @JsonProperty
    public abstract long count();

    public static TimerResponse create(double mean, double max, double min, double stddev, double percentile99, long count) {
        return builder()
                .mean(mean)
                .max(max)
                .min(min)
                .stddev(stddev)
                .percentile99(percentile99)
                .count(count)
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
