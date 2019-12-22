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

package horse.wtf.nzyme.alerts.service;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.alerts.Alert;
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
