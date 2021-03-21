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

package horse.wtf.nzyme.rest.responses.trackers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class TrackersListResponse {

    @JsonProperty("groundstation_enabled")
    public abstract boolean groundstationEnabled();

    @JsonProperty
    public abstract long total();

    @JsonProperty
    public abstract List<TrackerResponse> trackers();

    public static TrackersListResponse create(boolean groundstationEnabled, long total, List<TrackerResponse> trackers) {
        return builder()
                .groundstationEnabled(groundstationEnabled)
                .total(total)
                .trackers(trackers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrackersListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder groundstationEnabled(boolean groundstationEnabled);

        public abstract Builder total(long total);

        public abstract Builder trackers(List<TrackerResponse> trackers);

        public abstract TrackersListResponse build();
    }

}
