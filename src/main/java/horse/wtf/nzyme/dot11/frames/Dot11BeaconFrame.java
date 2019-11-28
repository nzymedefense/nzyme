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
import horse.wtf.nzyme.dot11.Dot11TaggedParameters;

import javax.annotation.Nullable;

@AutoValue
public abstract class Dot11BeaconFrame {

    @Nullable
    public abstract String ssid();

    public abstract String transmitter();

    public abstract String transmitterFingerprint();

    public abstract Dot11TaggedParameters taggedParameters();

    public abstract Dot11MetaInformation meta();

    public abstract byte[] payload();

    @JsonIgnore
    public String descriptionString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TYPE:        BEACON").append("\n");
        sb.append("SSID:        ").append(ssid()).append("\n");
        sb.append("Transmitter: ").append(transmitter()).append("\n");
        sb.append("Fingerprint: ").append(transmitterFingerprint()).append("\n");
        sb.append("WPS:         ").append(taggedParameters().isWPS()).append("\n");
        sb.append("Security:    ").append(taggedParameters().getFullSecurityString()).append("\n");

        return sb.toString();
    }

    public static Dot11BeaconFrame create(String ssid, String transmitter, String transmitterFingerprint, Dot11TaggedParameters taggedParameters, Dot11MetaInformation meta, byte[] payload) {
        return builder()
                .ssid(ssid)
                .transmitter(transmitter)
                .transmitterFingerprint(transmitterFingerprint)
                .taggedParameters(taggedParameters)
                .meta(meta)
                .payload(payload)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11BeaconFrame.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder transmitter(String transmitter);

        public abstract Builder transmitterFingerprint(String transmitterFingerprint);

        public abstract Builder taggedParameters(Dot11TaggedParameters taggedParameters);

        public abstract Builder meta(Dot11MetaInformation meta);

        public abstract Builder payload(byte[] payload);

        public abstract Dot11BeaconFrame build();
    }

}
