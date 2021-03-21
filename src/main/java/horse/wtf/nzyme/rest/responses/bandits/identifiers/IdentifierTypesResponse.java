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

package horse.wtf.nzyme.rest.responses.bandits.identifiers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class IdentifierTypesResponse {

    @JsonProperty
    public abstract long total();

    @JsonProperty
    public abstract List<String> types();

    public static IdentifierTypesResponse create(long total, List<String> types) {
        return builder()
                .total(total)
                .types(types)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_IdentifierTypesResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder types(List<String> types);

        public abstract IdentifierTypesResponse build();
    }

}
