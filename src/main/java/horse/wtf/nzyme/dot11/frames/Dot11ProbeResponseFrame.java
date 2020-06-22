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
public abstract class Dot11ProbeResponseFrame implements Dot11Frame {

    @Nullable
    public abstract String ssid();
    public abstract String destination();
    public abstract String transmitter();
    public abstract String transmitterFingerprint();
    public abstract Dot11TaggedParameters taggedParameters();
    public abstract Dot11MetaInformation meta();

    @JsonIgnore
    public String descriptionString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TYPE:        PROBE_RESPONSE").append("\n");
        sb.append("SSID:        ").append(ssid()).append("\n");
        sb.append("Destination: ").append(destination()).append("\n");
        sb.append("Transmitter: ").append(transmitter()).append("\n");
        sb.append("Fingerprint: ").append(transmitterFingerprint()).append("\n");
        sb.append("WPS:         ").append(taggedParameters().isWPS()).append("\n");
        sb.append("Security:    ").append(taggedParameters().getFullSecurityString()).append("\n");

        return sb.toString();
    }

    public static Dot11ProbeResponseFrame create(String ssid, String destination, String transmitter, String transmitterFingerprint, Dot11TaggedParameters taggedParameters, Dot11MetaInformation meta) {
        return builder()
                .ssid(ssid)
                .destination(destination)
                .transmitter(transmitter)
                .transmitterFingerprint(transmitterFingerprint)
                .taggedParameters(taggedParameters)
                .meta(meta)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11ProbeResponseFrame.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder destination(String destination);

        public abstract Builder transmitter(String transmitter);

        public abstract Builder transmitterFingerprint(String transmitterFingerprint);

        public abstract Builder taggedParameters(Dot11TaggedParameters taggedParameters);

        public abstract Builder meta(Dot11MetaInformation meta);

        public abstract Dot11ProbeResponseFrame build();
    }

}
