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

    @JsonProperty("mfa_valid")
    public abstract boolean mfaValid();

    @JsonProperty("mfa_setup")
    public abstract boolean mfaSetup();

    public static SessionInformationResponse create(boolean mfaValid, boolean mfaSetup) {
        return builder()
                .mfaValid(mfaValid)
                .mfaSetup(mfaSetup)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionInformationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder mfaValid(boolean mfaValid);

        public abstract Builder mfaSetup(boolean mfaSetup);

        public abstract SessionInformationResponse build();
    }
}
