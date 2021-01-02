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
import horse.wtf.nzyme.dot11.Dot11MetaInformation;

@AutoValue
public abstract class Dot11DisassociationFrame implements Dot11Frame {

    public abstract String destination();
    public abstract String transmitter();
    public abstract Short reasonCode();
    public abstract String reasonString();
    public abstract Dot11MetaInformation meta();

    @JsonIgnore
    public String descriptionString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TYPE:          DISASSOCIATION").append("\n");
        sb.append("Transmitter:   ").append(transmitter()).append("\n");
        sb.append("Destination:   ").append(destination()).append("\n");
        sb.append("Reason Code:   ").append(reasonCode()).append("\n");
        sb.append("Reason String: ").append(reasonString()).append("\n");

        return sb.toString();
    }

    public static Dot11DisassociationFrame create(String destination, String transmitter, Short reasonCode, String reasonString, Dot11MetaInformation meta) {
        return builder()
                .destination(destination)
                .transmitter(transmitter)
                .reasonCode(reasonCode)
                .reasonString(reasonString)
                .meta(meta)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11DisassociationFrame.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder destination(String destination);

        public abstract Builder transmitter(String transmitter);

        public abstract Builder reasonCode(Short reasonCode);

        public abstract Builder reasonString(String reasonString);

        public abstract Builder meta(Dot11MetaInformation meta);

        public abstract Dot11DisassociationFrame build();
    }

}
