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

package horse.wtf.nzyme.configuration;

import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

import java.util.List;

@AutoValue
public abstract class Dot11NetworkDefinition {

    public abstract String ssid();

    public abstract List<String> bssids();

    public abstract List<Integer> channels();

    public static Dot11NetworkDefinition create(String ssid, List<String> bssids, List<Integer> channels) {
        return builder()
                .ssid(ssid)
                .bssids(bssids)
                .channels(channels)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11NetworkDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder bssids(List<String> bssids);

        public abstract Builder channels(List<Integer> channels);

        public abstract Dot11NetworkDefinition build();
    }

    public static boolean checkConfig(Config c) {
        return !Strings.isNullOrEmpty(c.getString(Keys.SSID))
                && c.getStringList(Keys.BSSIDS) != null && !c.getStringList(Keys.BSSIDS).isEmpty()
                && c.getIntList(Keys.CHANNELS) != null && !c.getIntList(Keys.CHANNELS).isEmpty();
    }

}