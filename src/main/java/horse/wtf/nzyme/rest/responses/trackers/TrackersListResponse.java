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

import java.util.List;

@AutoValue
public abstract class TrackersListResponse {

    @JsonProperty
    public abstract long total();

    @JsonProperty
    public abstract List<TrackerResponse> trackers();

    public static TrackersListResponse create(long total, List<TrackerResponse> trackers) {
        return builder()
                .total(total)
                .trackers(trackers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrackersListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder trackers(List<TrackerResponse> trackers);

        public abstract TrackersListResponse build();
    }

}
