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

package app.nzyme.core.events.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class EventRecord {

    public abstract Long id();
    public abstract String type();
    public abstract String name();
    public abstract String description();
    public abstract DateTime createdAt();

    public static EventRecord create(Long id, String type, String name, String description, DateTime createdAt) {
        return builder()
                .id(id)
                .type(type)
                .name(name)
                .description(description)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_EventRecord.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(Long id);

        public abstract Builder type(String type);

        public abstract Builder name(String name);

        public abstract Builder description(String description);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract EventRecord build();
    }

}
