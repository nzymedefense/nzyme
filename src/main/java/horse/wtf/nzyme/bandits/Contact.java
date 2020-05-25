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

package horse.wtf.nzyme.bandits;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.bandits.engine.ContactIdentifier;
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

    @Nullable
    public abstract Long banditId();

    @Nullable
    public abstract Bandit bandit();

    public boolean isActive() {
        return lastSeen().isAfter(DateTime.now().minusMinutes(ContactIdentifier.ACTIVE_MINUTES));
    }

    public boolean isStationary() {
        // TODO
        return true;
    }

    public static Contact create(UUID uuid, DateTime firstSeen, DateTime lastSeen, Long frameCount, Role sourceRole, String sourceName, Long banditId, Bandit bandit) {
        return builder()
                .uuid(uuid)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .frameCount(frameCount)
                .sourceRole(sourceRole)
                .sourceName(sourceName)
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

        public abstract Builder banditId(Long banditId);

        public abstract Builder bandit(Bandit bandit);

        public abstract Contact build();
    }

}
