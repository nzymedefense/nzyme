/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.rest.responses.trackers;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.bandits.trackers.TrackerState;
import horse.wtf.nzyme.rest.responses.bandits.ContactResponse;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class TrackerResponse {

    @JsonProperty
    public abstract String name();

    @JsonProperty
    public abstract String version();

    @JsonProperty
    public abstract long drift();

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

    public static TrackerResponse create(String name, String version, long drift, DateTime lastSeen, TrackerState state, String trackingMode, List<ContactResponse> contacts, boolean hasPendingTrackingRequests, int rssi) {
        return builder()
                .name(name)
                .version(version)
                .drift(drift)
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

        public abstract Builder drift(long drift);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder state(TrackerState state);

        public abstract Builder trackingMode(String trackingMode);

        public abstract Builder contacts(List<ContactResponse> contacts);

        public abstract Builder hasPendingTrackingRequests(boolean hasPendingTrackingRequests);

        public abstract Builder rssi(int rssi);

        public abstract TrackerResponse build();
    }

}
