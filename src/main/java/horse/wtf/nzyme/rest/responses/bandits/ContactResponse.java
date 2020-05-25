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

package horse.wtf.nzyme.rest.responses.bandits;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class ContactResponse {

    @JsonProperty
    public abstract UUID uuid();

    @JsonProperty("frame_count")
    public abstract Long frameCount();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("is_active")
    public abstract Boolean isActive();

    @JsonProperty("source_role")
    public abstract String sourceRole();

    @JsonProperty("source_name")
    public abstract String sourceName();

    public static ContactResponse create(UUID uuid, Long frameCount, DateTime firstSeen, DateTime lastSeen, Boolean isActive, String sourceRole, String sourceName) {
        return builder()
                .uuid(uuid)
                .frameCount(frameCount)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .isActive(isActive)
                .sourceRole(sourceRole)
                .sourceName(sourceName)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ContactResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder frameCount(Long frameCount);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder isActive(Boolean isActive);

        public abstract Builder sourceRole(String sourceRole);

        public abstract Builder sourceName(String sourceName);

        public abstract ContactResponse build();
    }

}
