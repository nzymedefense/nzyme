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

package app.nzyme.core.dot11.networks.beaconrate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class AverageBeaconRate {

    @JsonProperty("rate")
    @Nullable
    public abstract Float rate();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static AverageBeaconRate create(Float rate, DateTime createdAt) {
        return builder()
                .rate(rate)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AverageBeaconRate.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder rate(Float rate);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract AverageBeaconRate build();
    }

}
