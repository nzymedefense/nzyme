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

package app.nzyme.core.dot11.networks.signalstrength.tracks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class SignalWaterfallHistogram {

    @JsonProperty("z")
    public abstract List<List<Long>> z();

    @JsonProperty("x")
    public abstract List<Integer> x();

    @JsonProperty("y")
    public abstract List<DateTime> y();

    @JsonCreator
    public static SignalWaterfallHistogram create(@JsonProperty("z") List<List<Long>> z,
                                                  @JsonProperty("x") List<Integer> x,
                                                  @JsonProperty("y") List<DateTime> y) {
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
        public abstract Builder z(List<List<Long>> z);

        public abstract Builder x(List<Integer> x);

        public abstract Builder y(List<DateTime> y);

        public abstract SignalWaterfallHistogram build();
    }
}
