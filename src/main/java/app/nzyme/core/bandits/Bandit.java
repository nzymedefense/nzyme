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

package app.nzyme.core.bandits;

import com.google.auto.value.AutoValue;
import app.nzyme.core.bandits.identifiers.BanditIdentifier;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class Bandit implements Identifiable {

    @Nullable
    public abstract Long databaseId();
    public abstract UUID uuid();
    public abstract String name();
    public abstract String description();

    public abstract Boolean readOnly();

    @Nullable
    public abstract DateTime createdAt();

    @Nullable
    public abstract DateTime updatedAt();

    @Nullable
    public abstract List<BanditIdentifier> identifiers();

    public static Bandit create(Long databaseId, UUID uuid, String name, String description, Boolean readOnly, DateTime createdAt, DateTime updatedAt, List<BanditIdentifier> identifiers) {
        return builder()
                .databaseId(databaseId)
                .uuid(uuid)
                .name(name)
                .description(description)
                .readOnly(readOnly)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .identifiers(identifiers)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Bandit.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder databaseId(Long databaseId);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder readOnly(Boolean readOnly);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder updatedAt(DateTime updatedAt);

        public abstract Builder identifiers(List<BanditIdentifier> identifiers);

        public abstract Bandit build();
    }
    
}
