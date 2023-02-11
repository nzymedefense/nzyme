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

package app.nzyme.core.rest.responses.trackers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.bandits.trackers.TrackerState;
import app.nzyme.core.rest.responses.bandits.ContactResponse;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TrackerResponse {

    @JsonProperty
    public abstract String name();

    @JsonProperty
    public abstract String version();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty
    public abstract TrackerState state();

    @JsonProperty("tracking_mode")
    public abstract String trackingMode();

    @JsonProperty("contacts")
    public abstract List<ContactResponse> contacts();

    @JsonProperty("has_pending_tracking_requests")
    public abstract boolean hasPendingTrackingRequests();

    @JsonProperty
    public abstract int rssi();

    public static TrackerResponse create(String name, String version, DateTime lastSeen, TrackerState state, String trackingMode, List<ContactResponse> contacts, boolean hasPendingTrackingRequests, int rssi) {
        return builder()
                .name(name)
                .version(version)
                .lastSeen(lastSeen)
                .state(state)
                .trackingMode(trackingMode)
                .contacts(contacts)
                .hasPendingTrackingRequests(hasPendingTrackingRequests)
                .rssi(rssi)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrackerResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder version(String version);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder state(TrackerState state);

        public abstract Builder trackingMode(String trackingMode);

        public abstract Builder contacts(List<ContactResponse> contacts);

        public abstract Builder hasPendingTrackingRequests(boolean hasPendingTrackingRequests);

        public abstract Builder rssi(int rssi);

        public abstract TrackerResponse build();
    }

}
