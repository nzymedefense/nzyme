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

package app.nzyme.core.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ChannelReport {

    public abstract String name();
    public abstract Long capacity();
    public abstract Long watermark();
    public abstract TotalWithAverage errors();
    public abstract TotalWithAverage throughputBytes();
    public abstract TotalWithAverage throughputMessages();

    @JsonCreator
    public static ChannelReport create(@JsonProperty("name") String name,
                                       @JsonProperty("capacity") Long capacity,
                                       @JsonProperty("watermark") Long watermark,
                                       @JsonProperty("errors") TotalWithAverage errors,
                                       @JsonProperty("throughput_bytes") TotalWithAverage throughputBytes,
                                       @JsonProperty("throughput_messages") TotalWithAverage throughputMessages) {
        return builder()
                .name(name)
                .capacity(capacity)
                .watermark(watermark)
                .errors(errors)
                .throughputBytes(throughputBytes)
                .throughputMessages(throughputMessages)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ChannelReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder capacity(Long capacity);

        public abstract Builder watermark(Long watermark);

        public abstract Builder errors(TotalWithAverage errors);

        public abstract Builder throughputBytes(TotalWithAverage throughputBytes);

        public abstract Builder throughputMessages(TotalWithAverage throughputMessages);

        public abstract ChannelReport build();
    }
}
