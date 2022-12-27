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

package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class CreateBanditIdentifierRequest {

    public abstract String type();
    public abstract Map<String, Object> configuration();

    @JsonCreator
    public static CreateBanditIdentifierRequest create(@JsonProperty("type") String type, @JsonProperty("configuration") Map<String, Object> configuration) {
        return builder()
                .type(type)
                .configuration(configuration)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateBanditIdentifierRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(String type);

        public abstract Builder configuration(Map<String, Object> configuration);

        public abstract CreateBanditIdentifierRequest build();
    }
}
