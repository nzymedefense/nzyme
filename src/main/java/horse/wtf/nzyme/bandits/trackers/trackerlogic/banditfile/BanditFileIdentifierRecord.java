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

package horse.wtf.nzyme.bandits.trackers.trackerlogic.banditfile;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class BanditFileIdentifierRecord {

    @JsonProperty("uuid")
    public abstract String uuid();

    @JsonProperty("type")
    public abstract String type();

    @JsonProperty("configuration")
    public abstract Map<String, Object> configuration();

    @JsonCreator
    public static BanditFileIdentifierRecord create(@JsonProperty("uuid") String uuid, @JsonProperty("type") String type, @JsonProperty("configuration") Map<String, Object> configuration) {
        return builder()
                .uuid(uuid)
                .type(type)
                .configuration(configuration)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BanditFileIdentifierRecord.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder uuid(String uuid);

        public abstract Builder type(String type);

        public abstract Builder configuration(Map<String, Object> configuration);

        public abstract BanditFileIdentifierRecord build();
    }

}
