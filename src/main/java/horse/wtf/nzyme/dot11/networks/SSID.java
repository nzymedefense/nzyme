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

package horse.wtf.nzyme.dot11.networks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import horse.wtf.nzyme.dot11.Dot11SecurityConfiguration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class SSID {

    public abstract String name();

    @JsonProperty
    public abstract Map<Integer, Channel> channels();

    @JsonProperty("human_readable")
    public boolean isHumanReadable() {
        for (char c : name().toCharArray()) {
            if (!Character.isISOControl(c) && !Character.isWhitespace(c)) {
                return true;
            }
        }

        return false;
    }

    @JsonProperty
    public List<Dot11SecurityConfiguration> security = Lists.newArrayList();

    @JsonProperty("name")
    public String nameSafe() {
        if (isHumanReadable()) {
            return name();
        } else {
            return "[not human readable]";
        }
    }

    @JsonIgnore
    public void updateSecurity(List<Dot11SecurityConfiguration> security) {
        this.security = security;
    }

    public static SSID create(String name) {
        return builder()
                .name(name)
                .channels(Maps.newHashMap())
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder channels(Map<Integer, Channel> channels);

        public abstract SSID build();
    }

}
