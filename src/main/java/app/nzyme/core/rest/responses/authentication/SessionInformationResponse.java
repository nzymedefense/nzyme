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

import app.nzyme.core.rest.responses.authentication.branding.BrandingResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;

@AutoValue
public abstract class SessionInformationResponse {

    @JsonProperty("user")
    public abstract SessionUserInformationDetailsResponse user();

    @JsonProperty("mfa_valid")
    public abstract boolean mfaValid();

    @JsonProperty("mfa_setup")
    public abstract boolean mfaSetup();

    @JsonProperty("mfa_entry_expires_at")
    @Nullable
    public abstract DateTime mfaEntryExpiresAt();

    @JsonProperty("branding")
    public abstract BrandingResponse branding();

    @JsonProperty("has_active_alerts")
    public abstract boolean hasActiveAlerts();

    @JsonProperty("health_indicator_level")
    @Nullable
    public abstract String healthIndicatorLevel();

    public static SessionInformationResponse create(SessionUserInformationDetailsResponse user, boolean mfaValid, boolean mfaSetup, DateTime mfaEntryExpiresAt, BrandingResponse branding, boolean hasActiveAlerts, String healthIndicatorLevel) {
        return builder()
                .user(user)
                .mfaValid(mfaValid)
                .mfaSetup(mfaSetup)
                .mfaEntryExpiresAt(mfaEntryExpiresAt)
                .branding(branding)
                .hasActiveAlerts(hasActiveAlerts)
                .healthIndicatorLevel(healthIndicatorLevel)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SessionInformationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder user(SessionUserInformationDetailsResponse user);

        public abstract Builder mfaValid(boolean mfaValid);

        public abstract Builder mfaSetup(boolean mfaSetup);

        public abstract Builder mfaEntryExpiresAt(DateTime mfaEntryExpiresAt);

        public abstract Builder branding(BrandingResponse branding);

        public abstract Builder hasActiveAlerts(boolean hasActiveAlerts);

        public abstract Builder healthIndicatorLevel(String healthIndicatorLevel);

        public abstract SessionInformationResponse build();
    }
}
