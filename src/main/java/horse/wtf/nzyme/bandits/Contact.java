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
import horse.wtf.nzyme.dot11.frames.Dot11Frame;
import org.joda.time.DateTime;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@AutoValue
public abstract class Contact {

    @JsonProperty
    public abstract UUID uuid();

    @JsonProperty
    public abstract Bandit bandit();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("frame_count")
    public abstract AtomicLong frameCount();

    @JsonProperty("is_stationary")
    public boolean isStationary() {
        // TODO
        return true;
    }

    @JsonIgnore
    public void recordFrame(Dot11Frame frame) {
        frameCount().incrementAndGet();
    }

    public static Contact create(UUID uuid, Bandit bandit, DateTime firstSeen, DateTime lastSeen, AtomicLong frameCount) {
        return builder()
                .uuid(uuid)
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

        public abstract Builder frameCount(AtomicLong frameCount);

        public abstract Contact build();
    }
}
