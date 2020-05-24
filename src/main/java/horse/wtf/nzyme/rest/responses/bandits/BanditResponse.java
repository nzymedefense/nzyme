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

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class BanditResponse {

    @JsonProperty
    public abstract UUID uuid();

    @JsonProperty("database_id")
    public abstract long databaseId();

    @JsonProperty
    public abstract String name();

    @JsonProperty
    public abstract String description();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("updated_at")
    public abstract DateTime updatedAt();

    @JsonProperty("read_only")
    public abstract Boolean readOnly();

    @JsonProperty("last_contact")
    @Nullable
    public abstract DateTime lastContact();

    @JsonProperty("is_active")
    public abstract Boolean isActive();

    @JsonProperty
    public abstract List<BanditIdentifierResponse> identifiers();

    @JsonProperty
    public abstract List<ContactResponse> contacts();

    public static BanditResponse create(UUID uuid, long databaseId, String name, String description, DateTime createdAt, DateTime updatedAt, Boolean readOnly, DateTime lastContact, Boolean isActive, List<BanditIdentifierResponse> identifiers, List<ContactResponse> contacts) {
        return builder()
                .uuid(uuid)
                .databaseId(databaseId)
                .name(name)
                .description(description)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .readOnly(readOnly)
                .lastContact(lastContact)
                .isActive(isActive)
                .identifiers(identifiers)
                .contacts(contacts)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BanditResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder databaseId(long databaseId);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder readOnly(Boolean readOnly);

        public abstract Builder lastContact(DateTime lastContact);

        public abstract Builder isActive(Boolean isActive);

        public abstract Builder identifiers(List<BanditIdentifierResponse> identifiers);

        public abstract Builder contacts(List<ContactResponse> contacts);

        public abstract BanditResponse build();
    }

}
