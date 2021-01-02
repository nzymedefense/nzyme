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
import horse.wtf.nzyme.dot11.parsers.Dot11AuthenticationFrameParser;

@AutoValue
public abstract class Dot11AuthenticationFrame implements Dot11Frame {

    public abstract Dot11AuthenticationFrameParser.ALGORITHM_TYPE algorithm();
    public abstract Short statusCode();
    public abstract String statusString();
    public abstract Short transactionSequence();
    public abstract String destination();
    public abstract String transmitter();
    public abstract Dot11MetaInformation meta();

    @JsonIgnore
    public String descriptionString() {
        StringBuilder sb = new StringBuilder();

        sb.append("TYPE:            AUTHENTICATION").append("\n");
        sb.append("Transmitter:     ").append(transmitter()).append("\n");
        sb.append("Destination:     ").append(destination()).append("\n");
        sb.append("Status String:   ").append(statusString()).append("\n");
        sb.append("Status Code:     ").append(statusCode()).append("\n");
        sb.append("Transaction Seq: ").append(transactionSequence()).append("\n");
        sb.append("Algorithm:       ").append(algorithm()).append("\n");

        return sb.toString();
    }

    public static Dot11AuthenticationFrame create(Dot11AuthenticationFrameParser.ALGORITHM_TYPE algorithm, Short statusCode, String statusString, Short transactionSequence, String destination, String transmitter, Dot11MetaInformation meta) {
        return builder()
                .algorithm(algorithm)
                .statusCode(statusCode)
                .statusString(statusString)
                .transactionSequence(transactionSequence)
                .destination(destination)
                .transmitter(transmitter)
                .meta(meta)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11AuthenticationFrame.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder algorithm(Dot11AuthenticationFrameParser.ALGORITHM_TYPE algorithm);

        public abstract Builder statusCode(Short statusCode);

        public abstract Builder statusString(String statusString);

        public abstract Builder transactionSequence(Short transactionSequence);

        public abstract Builder destination(String destination);

        public abstract Builder transmitter(String transmitter);

        public abstract Builder meta(Dot11MetaInformation meta);

        public abstract Dot11AuthenticationFrame build();
    }

}
