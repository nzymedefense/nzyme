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

package app.nzyme.core.rest.responses.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ChannelStatisticsResponse {
    @JsonProperty("total_frames")
    public abstract Long totalFrames();

    @JsonProperty("malformed_frames")
    public abstract Long malformedFrames();

    public static ChannelStatisticsResponse create(Long totalFrames, Long malformedFrames) {
        return builder()
                .totalFrames(totalFrames)
                .malformedFrames(malformedFrames)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ChannelStatisticsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder totalFrames(Long totalFrames);

        public abstract Builder malformedFrames(Long malformedFrames);

        public abstract ChannelStatisticsResponse build();
    }

}
