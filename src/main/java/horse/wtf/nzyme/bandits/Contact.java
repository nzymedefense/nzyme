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

package horse.wtf.nzyme.bandits;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.bandits.engine.ContactManager;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class Contact {

    public abstract UUID uuid();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract Long frameCount();
    public abstract Role sourceRole();
    public abstract String sourceName();
    public abstract int lastSignal();

    @Nullable
    public abstract Long banditId();

    @Nullable
    public abstract Bandit bandit();

    public boolean isActive() {
        return lastSeen().isAfter(DateTime.now().minusMinutes(TrackTimeout.MINUTES));
    }

    public static Contact create(UUID uuid, DateTime firstSeen, DateTime lastSeen, Long frameCount, Role sourceRole, String sourceName, int lastSignal, Long banditId, Bandit bandit) {
        return builder()
                .uuid(uuid)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .frameCount(frameCount)
                .sourceRole(sourceRole)
                .sourceName(sourceName)
                .lastSignal(lastSignal)
                .banditId(banditId)
                .bandit(bandit)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Contact.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder frameCount(Long frameCount);

        public abstract Builder sourceRole(Role sourceRole);

        public abstract Builder sourceName(String sourceName);

        public abstract Builder lastSignal(int lastSignal);

        public abstract Builder banditId(Long banditId);

        public abstract Builder bandit(Bandit bandit);

        public abstract Contact build();
    }
}
