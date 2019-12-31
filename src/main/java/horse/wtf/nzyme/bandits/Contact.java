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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import javax.annotation.Nullable;
import java.util.UUID;

@AutoValue
public abstract class Contact {

    public abstract UUID uuid();
    public abstract DateTime firstSeen();
    public abstract DateTime lastSeen();
    public abstract Long frameCount();

    @Nullable
    public abstract Long banditId();

    @Nullable
    public abstract Bandit bandit();

    public boolean isStationary() {
        // TODO
        return true;
    }

    public static Contact create(UUID uuid, Long banditId, Bandit bandit, DateTime firstSeen, DateTime lastSeen, Long frameCount) {
        return builder()
                .uuid(uuid)
                .banditId(banditId)
                .bandit(bandit)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Contact.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(UUID uuid);

        public abstract Builder bandit(Bandit bandit);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder frameCount(Long frameCount);

        public abstract Builder banditId(Long banditId);

        public abstract Contact build();
    }
}
