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

package horse.wtf.nzyme.dot11.frames;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;

@AutoValue
public abstract class Dot11DeauthenticationFrame extends Dot11Frame {

    public abstract String destination();
    public abstract String transmitter();
    public abstract String bssid();
    public abstract Short reasonCode();
    public abstract String reasonString();
    public abstract Dot11MetaInformation meta();

    @JsonIgnore
    public String descriptionString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TYPE:          DEAUTHENTICATION").append("\n");
        sb.append("Transmitter:   ").append(transmitter()).append("\n");
        sb.append("Destination:   ").append(destination()).append("\n");
        sb.append("BSSID:         ").append(bssid()).append("\n");
        sb.append("Reason Code:   ").append(reasonCode()).append("\n");
        sb.append("Reason String: ").append(reasonString()).append("\n");

        return sb.toString();
    }

    public static Dot11DeauthenticationFrame create(String destination, String transmitter, String bssid, Short reasonCode, String reasonString, Dot11MetaInformation meta) {
        return builder()
                .destination(destination)
                .transmitter(transmitter)
                .bssid(bssid)
                .reasonCode(reasonCode)
                .reasonString(reasonString)
                .meta(meta)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11DeauthenticationFrame.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder destination(String destination);

        public abstract Builder transmitter(String transmitter);

        public abstract Builder bssid(String bssid);

        public abstract Builder reasonCode(Short reasonCode);

        public abstract Builder reasonString(String reasonString);

        public abstract Builder meta(Dot11MetaInformation meta);

        public abstract Dot11DeauthenticationFrame build();
    }

}
