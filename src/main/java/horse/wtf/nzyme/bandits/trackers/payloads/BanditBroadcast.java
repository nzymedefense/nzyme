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

package horse.wtf.nzyme.bandits.trackers.payloads;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class BanditBroadcast {

    @JsonProperty
    public abstract UUID uuid();

    @JsonProperty
    public abstract String name();

    @JsonProperty
    public abstract String description();

    @JsonProperty("contact_identifiers")
    public abstract List<BanditIdentifierBroadcast> contactIdentifiers();

    @JsonCreator
    public static BanditBroadcast create(@JsonProperty("uuid") UUID uuid,
                                         @JsonProperty("name") String name,
                                         @JsonProperty("description") String description,
                                         @JsonProperty("contact_identifiers") List<BanditIdentifierBroadcast> contactIdentifiers) {
        return builder()
                .uuid(uuid)
                .name(name)
                .description(description)
                .contactIdentifiers(contactIdentifiers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BanditBroadcast.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder contactIdentifiers(List<BanditIdentifierBroadcast> contactIdentifiers);

        public abstract BanditBroadcast build();
    }

}
