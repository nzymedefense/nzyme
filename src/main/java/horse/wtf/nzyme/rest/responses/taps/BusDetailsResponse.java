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

import java.util.List;

@AutoValue
public abstract class BusDetailsResponse {

    @JsonProperty("id")
    public abstract Long id();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("channels")
    public abstract List<ChannelDetailsResponse> channels();

    public static BusDetailsResponse create(Long id, String name, List<ChannelDetailsResponse> channels) {
        return builder()
                .id(id)
                .name(name)
                .channels(channels)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BusDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder name(String name);

        public abstract Builder channels(List<ChannelDetailsResponse> channels);

        public abstract BusDetailsResponse build();
    }

}
