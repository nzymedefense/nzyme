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

package horse.wtf.nzyme.rest.responses.taps;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ChannelDetailsResponse {

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("capacity")
    public abstract Long capacity();

    @JsonProperty("watermark")
    public abstract Long watermark();

    @JsonProperty("errors")
    public abstract TotalWithAverageResponse errors();

    @JsonProperty("throughput_bytes")
    public abstract TotalWithAverageResponse throughputBytes();

    @JsonProperty("throughput_messages")
    public abstract TotalWithAverageResponse throughputMessages();

    public static ChannelDetailsResponse create(String name, Long capacity, Long watermark, TotalWithAverageResponse errors, TotalWithAverageResponse throughputBytes, TotalWithAverageResponse throughputMessages) {
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
        return new AutoValue_ChannelDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder capacity(Long capacity);

        public abstract Builder watermark(Long watermark);

        public abstract Builder errors(TotalWithAverageResponse errors);

        public abstract Builder throughputBytes(TotalWithAverageResponse throughputBytes);

        public abstract Builder throughputMessages(TotalWithAverageResponse throughputMessages);

        public abstract ChannelDetailsResponse build();
    }
}
