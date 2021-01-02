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
public abstract class Dot11AssociationResponseFrame implements Dot11Frame {

    public abstract String transmitter();
    public abstract String destination();
    public abstract String response();
    public abstract Short responseCode();
    public abstract Dot11MetaInformation meta();

    @JsonIgnore
    public String descriptionString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TYPE:          ASSOCIATION RESPONSE").append("\n");
        sb.append("Transmitter:   ").append(transmitter()).append("\n");
        sb.append("Destination:   ").append(destination()).append("\n");
        sb.append("Response:      ").append(response()).append("\n");
        sb.append("Response Code: ").append(responseCode()).append("\n");

        return sb.toString();
    }

    public static Dot11AssociationResponseFrame create(String transmitter, String destination, String response, Short responseCode, Dot11MetaInformation meta) {
        return builder()
                .transmitter(transmitter)
                .destination(destination)
                .response(response)
                .responseCode(responseCode)
                .meta(meta)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11AssociationResponseFrame.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder transmitter(String transmitter);

        public abstract Builder destination(String destination);

        public abstract Builder response(String response);

        public abstract Builder responseCode(Short responseCode);

        public abstract Builder meta(Dot11MetaInformation meta);

        public abstract Dot11AssociationResponseFrame build();
    }

}
