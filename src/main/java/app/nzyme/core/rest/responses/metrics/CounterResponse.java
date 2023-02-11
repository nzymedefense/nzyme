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

import com.codahale.metrics.Counter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CounterResponse {

    @JsonProperty("count")
    public abstract long count();

    public static CounterResponse fromCounter(Counter counter) {
        return CounterResponse.create(counter.getCount());
    }

    public static CounterResponse create(long count) {
        return builder()
                .count(count)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CounterResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract CounterResponse build();
    }

}
