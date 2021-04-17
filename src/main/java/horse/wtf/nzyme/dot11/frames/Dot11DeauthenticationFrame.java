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

package horse.wtf.nzyme.dot11.frames;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.dot11.Dot11FrameSubtype;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;

@AutoValue
public abstract class Dot11DeauthenticationFrame implements Dot11Frame {

    public abstract String destination();
    public abstract String transmitter();
    public abstract String bssid();
    public abstract Short reasonCode();
    public abstract String reasonString();
    public abstract Dot11MetaInformation meta();
    public abstract byte[] payload();
    public abstract byte[] header();
    public abstract byte frameType();

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

    public static Dot11DeauthenticationFrame create(String destination, String transmitter, String bssid, Short reasonCode, String reasonString, Dot11MetaInformation meta, byte[] payload, byte[] header) {
        return builder()
                .destination(destination)
                .transmitter(transmitter)
                .bssid(bssid)
                .reasonCode(reasonCode)
                .reasonString(reasonString)
                .meta(meta)
                .payload(payload)
                .header(header)
                .frameType(Dot11FrameSubtype.DEAUTHENTICATION)
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

        public abstract Builder payload(byte[] payload);

        public abstract Builder header(byte[] header);

        public abstract Builder frameType(byte type);

        public abstract Dot11DeauthenticationFrame build();
    }

}
