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

package app.nzyme.core.dot11.frames;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.value.AutoValue;
import app.nzyme.core.dot11.Dot11FrameSubtype;
import app.nzyme.core.dot11.Dot11MetaInformation;
import app.nzyme.core.dot11.Dot11TaggedParameters;

import javax.annotation.Nullable;

@AutoValue
public abstract class Dot11BeaconFrame implements Dot11Frame {

    @Nullable
    public abstract String ssid();
    public abstract String transmitter();
    public abstract String transmitterFingerprint();
    public abstract Dot11TaggedParameters taggedParameters();
    public abstract Dot11MetaInformation meta();
    public abstract byte[] payload();
    public abstract byte[] header();
    public abstract byte frameType();

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

    public static Dot11BeaconFrame create(String ssid, String transmitter, String transmitterFingerprint, Dot11TaggedParameters taggedParameters, Dot11MetaInformation meta, byte[] payload, byte[] header) {
        return builder()
                .ssid(ssid)
                .transmitter(transmitter)
                .transmitterFingerprint(transmitterFingerprint)
                .taggedParameters(taggedParameters)
                .meta(meta)
                .payload(payload)
                .header(header)
                .frameType(Dot11FrameSubtype.BEACON)
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

        public abstract Builder header(byte[] header);

        public abstract Builder frameType(byte type);

        public abstract Dot11BeaconFrame build();
    }

}
