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

package horse.wtf.nzyme.dot11;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Dot11SecurityConfiguration {

    public enum MODE {
        WEP,
        WPA_1,
        WPA_2
    }

    public enum KEY_MGMT_MODE {
        PSK,
        EAM,
        UNKNOWN
    }

    public enum ENCRYPTION_MODE {
        TKIP,
        CCMP, // AES
        UNKNOWN
    }

    public abstract MODE wpaMode();
    public abstract List<KEY_MGMT_MODE> keyManagementModes();
    public abstract List<ENCRYPTION_MODE> encryptionModes();

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
