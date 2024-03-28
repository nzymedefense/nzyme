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

package app.nzyme.core.rest.responses.taps.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TapMetricsHistogramValueResponse {

    @JsonProperty("timestamp")
    public abstract DateTime timestamp();

    @JsonProperty("average")
    public abstract Double average();

    @JsonProperty("maximum")
    public abstract Double maximum();

    @JsonProperty("minimum")
    public abstract Double minimum();

    public static TapMetricsHistogramValueResponse create(DateTime timestamp, Double average, Double maximum, Double minimum) {
        return builder()
                .timestamp(timestamp)
                .average(average)
                .maximum(maximum)
                .minimum(minimum)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMetricsHistogramValueResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder average(Double average);

        public abstract Builder maximum(Double maximum);

        public abstract Builder minimum(Double minimum);

        public abstract TapMetricsHistogramValueResponse build();
    }
}
