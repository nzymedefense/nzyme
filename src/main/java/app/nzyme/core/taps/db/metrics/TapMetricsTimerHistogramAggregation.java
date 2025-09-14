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

package app.nzyme.core.taps.db.metrics;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class TapMetricsTimerHistogramAggregation {

    public abstract DateTime bucket();
    public abstract Double average();
    public abstract Double maximum();
    public abstract Double minimum();

    public static TapMetricsTimerHistogramAggregation create(DateTime bucket, Double average, Double maximum, Double minimum) {
        return builder()
                .bucket(bucket)
                .average(average)
                .maximum(maximum)
                .minimum(minimum)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMetricsTimerHistogramAggregation.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bucket(DateTime bucket);

        public abstract Builder average(Double average);

        public abstract Builder maximum(Double maximum);

        public abstract Builder minimum(Double minimum);

        public abstract TapMetricsTimerHistogramAggregation build();
    }
}
