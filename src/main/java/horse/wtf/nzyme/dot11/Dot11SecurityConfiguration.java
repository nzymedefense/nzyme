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

package horse.wtf.nzyme.dot11;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Dot11SecurityConfiguration {

    public enum MODE {
        NONE,
        WPA1,
        WPA2,
        WPA3
    }

    public enum KEY_MGMT_MODE {
        PSK,
        EAM,
        PSKSHA256,
        SAE,
        UNKNOWN
    }

    public enum ENCRYPTION_MODE {
        TKIP,
        CCMP, // AES
        UNKNOWN
    }

    @JsonProperty("wpa_mode")
    public abstract MODE wpaMode();

    @JsonProperty("key_mgmt_modes")
    public abstract List<KEY_MGMT_MODE> keyManagementModes();

    @JsonProperty("encryption_modes")
    public abstract List<ENCRYPTION_MODE> encryptionModes();

    @JsonProperty("as_string")
    public String asString() {
        StringBuilder sb = new StringBuilder();

        sb.append(wpaMode());
        for (KEY_MGMT_MODE managementMode : keyManagementModes()) {
            sb.append("-").append(managementMode);
        }

        for (ENCRYPTION_MODE encryptionMode : encryptionModes()) {
            sb.append("-").append(encryptionMode);
        }

        return sb.toString();
    }

    public static Dot11SecurityConfiguration create(MODE wpaMode, List<KEY_MGMT_MODE> keyManagementModes, List<ENCRYPTION_MODE> encryptionModes) {
        return builder()
                .wpaMode(wpaMode)
                .keyManagementModes(keyManagementModes)
                .encryptionModes(encryptionModes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11SecurityConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder wpaMode(MODE wpaMode);

        public abstract Builder keyManagementModes(List<KEY_MGMT_MODE> keyManagementModes);

        public abstract Builder encryptionModes(List<ENCRYPTION_MODE> encryptionModes);

        public abstract Dot11SecurityConfiguration build();
    }

}
