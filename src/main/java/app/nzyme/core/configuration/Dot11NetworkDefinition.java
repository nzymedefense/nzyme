/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package app.nzyme.core.configuration;

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

    public abstract int beaconRate();

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

    public static Dot11NetworkDefinition create(String ssid, List<Dot11BSSIDDefinition> bssids, List<Integer> channels, List<String> security, int beaconRate) {
        return builder()
                .ssid(ssid)
                .bssids(bssids)
                .channels(channels)
                .security(security)
                .beaconRate(beaconRate)
                .build();
    }

    @JsonIgnore
    public static boolean checkConfig(Config c) {
        return !Strings.isNullOrEmpty(c.getString(ConfigurationKeys.SSID))
                && c.getConfigList(ConfigurationKeys.BSSIDS) != null && !c.getConfigList(ConfigurationKeys.BSSIDS).isEmpty()
                && c.getIntList(ConfigurationKeys.CHANNELS) != null && !c.getIntList(ConfigurationKeys.CHANNELS).isEmpty()
                && c.getStringList(ConfigurationKeys.SECURITY) != null
                && c.getInt(ConfigurationKeys.BEACON_RATE) >= 0;
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

        public abstract Builder beaconRate(int beaconRate);

        public abstract Dot11NetworkDefinition build();
    }

}