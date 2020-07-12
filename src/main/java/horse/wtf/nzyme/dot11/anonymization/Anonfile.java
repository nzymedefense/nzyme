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

package horse.wtf.nzyme.dot11.anonymization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Anonfile {

    @JsonProperty("ssids")
    public abstract Map<String,String> ssids();

    @JsonProperty("bssids")
    public abstract Map<String,String> bssids();

    @JsonCreator
    public static Anonfile create(@JsonProperty("ssids") Map<String, String> ssids, @JsonProperty("bssids") Map<String, String> bssids) {
        return builder()
                .ssids(ssids)
                .bssids(bssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Anonfile.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssids(Map<String, String> ssids);

        public abstract Builder bssids(Map<String, String> bssids);

        public abstract Anonfile build();
    }

}
