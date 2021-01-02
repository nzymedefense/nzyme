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

package horse.wtf.nzyme.rest.responses.bandits;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class BanditIdentifierResponse {

    @JsonProperty
    public abstract Map<String, Object> configuration();

    @JsonProperty
    public abstract BanditIdentifier.TYPE type();

    @JsonProperty
    public abstract String description();

    @JsonProperty
    public abstract String matches();

    @JsonProperty
    public abstract long databaseId();

    @JsonProperty
    public abstract UUID uuid();

    public static BanditIdentifierResponse create(Map<String, Object> configuration, BanditIdentifier.TYPE type, String description, String matches, long databaseId, UUID uuid) {
        return builder()
                .configuration(configuration)
                .type(type)
                .description(description)
                .matches(matches)
                .databaseId(databaseId)
                .uuid(uuid)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BanditIdentifierResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder configuration(Map<String, Object> configuration);

        public abstract Builder type(BanditIdentifier.TYPE type);

        public abstract Builder description(String description);

        public abstract Builder matches(String matches);

        public abstract Builder databaseId(long databaseId);

        public abstract Builder uuid(UUID uuid);

        public abstract BanditIdentifierResponse build();
    }

}
