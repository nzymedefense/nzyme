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

package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class TrapResponse {

    @JsonProperty("probe")
    public abstract ProbeResponse probe();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("description")
    public abstract String description();

    public static TrapResponse create(ProbeResponse probe, String type, String description) {
        return builder()
                .probe(probe)
                .type(type)
                .description(description)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrapResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder probe(ProbeResponse probe);

        public abstract Builder type(String type);

        public abstract Builder description(String description);

        public abstract TrapResponse build();
    }

}
