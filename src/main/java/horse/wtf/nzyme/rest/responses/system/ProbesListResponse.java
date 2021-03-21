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

package horse.wtf.nzyme.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ProbesListResponse {

    @JsonProperty("total")
    public abstract int total();

    @JsonProperty("probes")
    public abstract List<ProbeResponse> probes();

    public static ProbesListResponse create(int total, List<ProbeResponse> probes) {
        return builder()
                .total(total)
                .probes(probes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ProbesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(int total);

        public abstract Builder probes(List<ProbeResponse> probes);

        public abstract ProbesListResponse build();
    }

}
