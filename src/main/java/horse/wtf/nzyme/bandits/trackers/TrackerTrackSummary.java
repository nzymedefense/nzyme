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
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class TrackerTrackSummary {

    public abstract UUID track();
    public abstract DateTime lastContact();
    public abstract int lastSignal();
    public abstract long frameCount();

    public static TrackerTrackSummary create(UUID track, DateTime lastContact, int lastSignal, long frameCount) {
        return builder()
                .track(track)
                .lastContact(lastContact)
                .lastSignal(lastSignal)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrackerTrackSummary.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder track(UUID track);

        public abstract Builder lastContact(DateTime lastContact);

        public abstract Builder lastSignal(int lastSignal);

        public abstract Builder frameCount(long frameCount);

        public abstract TrackerTrackSummary build();
    }

}
