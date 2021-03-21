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

package horse.wtf.nzyme.rest.responses.networks;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.dot11.Dot11SecurityConfiguration;

import java.util.List;

@AutoValue
public abstract class SSIDSecurityResponse {

    @JsonProperty("wpa_mode")
    public abstract Dot11SecurityConfiguration.MODE wpaMode();

    @JsonProperty("key_mgmt_modes")
    public abstract List<Dot11SecurityConfiguration.KEY_MGMT_MODE> keyManagementModes();

    @JsonProperty("encryption_modes")
    public abstract List<Dot11SecurityConfiguration.ENCRYPTION_MODE> encryptionModes();

    @JsonProperty("as_string")
    public abstract String asString();

    public static SSIDSecurityResponse create(Dot11SecurityConfiguration.MODE wpaMode, List<Dot11SecurityConfiguration.KEY_MGMT_MODE> keyManagementModes, List<Dot11SecurityConfiguration.ENCRYPTION_MODE> encryptionModes, String asString) {
        return builder()
                .wpaMode(wpaMode)
                .keyManagementModes(keyManagementModes)
                .encryptionModes(encryptionModes)
                .asString(asString)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDSecurityResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder wpaMode(Dot11SecurityConfiguration.MODE wpaMode);

        public abstract Builder keyManagementModes(List<Dot11SecurityConfiguration.KEY_MGMT_MODE> keyManagementModes);

        public abstract Builder encryptionModes(List<Dot11SecurityConfiguration.ENCRYPTION_MODE> encryptionModes);

        public abstract Builder asString(String asString);

        public abstract SSIDSecurityResponse build();
    }

}
