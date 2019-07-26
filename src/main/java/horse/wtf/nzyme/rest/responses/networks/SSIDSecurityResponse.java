/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
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
