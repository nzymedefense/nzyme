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

package horse.wtf.nzyme.dot11.networks;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class SecurityConfiguration {

    public enum WPA_MODE {
        WPA_1,
        WPA_2
    }

    public enum KEY_MGMT_MODE {
        PSK,
        EAM
    }

    public enum ENCRYPTION_MODE {
        TKIP,
        CCMP // AES
    }

    public abstract boolean hasWEP();
    public abstract List<WPA_MODE> wpaModes();
    public abstract List<KEY_MGMT_MODE> keyManagementModes();
    public abstract List<ENCRYPTION_MODE> encryptionModes();

    public static SecurityConfiguration create(boolean hasWEP, List<WPA_MODE> wpaModes, List<KEY_MGMT_MODE> keyManagementModes, List<ENCRYPTION_MODE> encryptionModes) {
        return builder()
                .hasWEP(hasWEP)
                .wpaModes(wpaModes)
                .keyManagementModes(keyManagementModes)
                .encryptionModes(encryptionModes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SecurityConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder hasWEP(boolean hasWEP);

        public abstract Builder wpaModes(List<WPA_MODE> wpaModes);

        public abstract Builder keyManagementModes(List<KEY_MGMT_MODE> keyManagementModes);

        public abstract Builder encryptionModes(List<ENCRYPTION_MODE> encryptionModes);

        public abstract SecurityConfiguration build();
    }

}
