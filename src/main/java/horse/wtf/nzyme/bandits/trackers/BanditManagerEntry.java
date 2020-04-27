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

package horse.wtf.nzyme.bandits.trackers;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.bandits.Bandit;
import org.joda.time.DateTime;

@AutoValue
public abstract class BanditManagerEntry {

    public abstract Bandit bandit();
    public abstract DateTime receivedAt();

    public static BanditManagerEntry create(Bandit bandit, DateTime receivedAt) {
        return builder()
                .bandit(bandit)
                .receivedAt(receivedAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BanditManagerEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bandit(Bandit bandit);

        public abstract Builder receivedAt(DateTime receivedAt);

        public abstract BanditManagerEntry build();
    }

}
