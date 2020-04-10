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
import org.joda.time.DateTime;


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

    public static TrackerResponse create(String name, String version, long drift, DateTime lastSeen, TrackerState state) {
        return builder()
                .name(name)
                .version(version)
                .drift(drift)
                .lastSeen(lastSeen)
                .state(state)
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

        public abstract TrackerResponse build();
    }

}
