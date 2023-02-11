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

package app.nzyme.core.alerts.service;

import com.google.auto.value.AutoValue;
import app.nzyme.core.Subsystem;
import app.nzyme.core.alerts.Alert;
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class AlertDatabaseEntry {

    public abstract long id();
    public abstract UUID uuid();
    public abstract Alert.TYPE type();
    public abstract Subsystem subsystem();
    public abstract String fields();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract long frameCount();

    public static AlertDatabaseEntry create(long id, UUID uuid, Alert.TYPE type, Subsystem subsystem, String fields, DateTime firstSeen, DateTime lastSeen, long frameCount) {
        return builder()
                .id(id)
                .uuid(uuid)
                .type(type)
                .subsystem(subsystem)
                .fields(fields)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AlertDatabaseEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(long id);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder type(Alert.TYPE type);

        public abstract Builder subsystem(Subsystem subsystem);

        public abstract Builder fields(String fields);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder frameCount(long frameCount);

        public abstract AlertDatabaseEntry build();
    }

}
