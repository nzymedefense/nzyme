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

package app.nzyme.core.rest.responses.authentication;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SessionInformationResponse {

    public enum STATUS {
        VALID, INVALID
    }

    @JsonProperty
    public abstract STATUS status();

    @JsonProperty("seconds_left_valid")
    public abstract int secondsLeftValid();

    public static SessionInformationResponse create(STATUS status, int secondsLeftValid) {
        return builder()
                .status(status)
                .secondsLeftValid(secondsLeftValid)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionInformationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder status(STATUS status);

        public abstract Builder secondsLeftValid(int secondsLeftValid);

        public abstract SessionInformationResponse build();
    }

}
