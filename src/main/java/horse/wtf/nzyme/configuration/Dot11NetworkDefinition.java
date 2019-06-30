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

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

import java.util.Collections;
import java.util.List;

@AutoValue
public abstract class Dot11NetworkDefinition {

    public abstract String ssid();

    public abstract List<Dot11BSSIDDefinition> bssids();

    public abstract List<Integer> channels();

    public abstract List<String> security();

    @JsonIgnore
    public List<String> allBSSIDAddresses() {
        if (bssids() == null || bssids().isEmpty()) {
            return Collections.emptyList();
        } else {
            List<String> addresses = Lists.newArrayList();
            for (Dot11BSSIDDefinition bssid : bssids()) {
                addresses.add(bssid.address());
            }

            return addresses;
        }
    }

    public static Dot11NetworkDefinition create(String ssid, List<Dot11BSSIDDefinition> bssids, List<Integer> channels, List<String> security) {
        return builder()
                .ssid(ssid)
                .bssids(bssids)
                .channels(channels)
                .security(security)
                .build();
    }

    @JsonIgnore
    public static boolean checkConfig(Config c) {
        return !Strings.isNullOrEmpty(c.getString(ConfigurationKeys.SSID))
                && c.getConfigList(ConfigurationKeys.BSSIDS) != null && !c.getConfigList(ConfigurationKeys.BSSIDS).isEmpty()
                && c.getIntList(ConfigurationKeys.CHANNELS) != null && !c.getIntList(ConfigurationKeys.CHANNELS).isEmpty()
                && c.getStringList(ConfigurationKeys.SECURITY) != null;
    }

    public static Builder builder() {
        return new AutoValue_Dot11NetworkDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder bssids(List<Dot11BSSIDDefinition> bssids);

        public abstract Builder channels(List<Integer> channels);

        public abstract Builder security(List<String> security);

        public abstract Dot11NetworkDefinition build();
    }

}