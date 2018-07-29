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

package horse.wtf.nzyme.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.Subsystem;
import horse.wtf.nzyme.alerts.Alert;
import org.joda.time.DateTime;

import java.util.Map;
import java.util.UUID;

@AutoValue
public abstract class AlertDetailsResponse {

    @JsonProperty("subsystem")
    public abstract Subsystem subsystem();

    @JsonProperty("type")
    public abstract Alert.Type type();

    @JsonProperty("uuid")
    public abstract UUID uuid();

    @JsonProperty("message")
    public abstract String message();

    @JsonProperty("fields")
    public abstract Map<String, Object> fields();

    @JsonProperty("first_seen")
    public abstract DateTime firstSeen();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    @JsonProperty("frame_count")
    public abstract Long frameCount();

    public static AlertDetailsResponse create(Subsystem subsystem, Alert.Type type, UUID uuid, String message, Map<String, Object> fields, DateTime firstSeen, DateTime lastSeen, Long frameCount) {
        return builder()
                .subsystem(subsystem)
                .type(type)
                .uuid(uuid)
                .message(message)
                .fields(fields)
                .firstSeen(firstSeen)
                .lastSeen(lastSeen)
                .frameCount(frameCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AlertDetailsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder subsystem(Subsystem subsystem);

        public abstract Builder type(Alert.Type type);

        public abstract Builder uuid(UUID uuid);

        public abstract Builder message(String message);

        public abstract Builder fields(Map<String, Object> fields);

        public abstract Builder firstSeen(DateTime firstSeen);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract Builder frameCount(Long frameCount);

        public abstract AlertDetailsResponse build();
    }

}
