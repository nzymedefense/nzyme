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

package horse.wtf.nzyme.dot11.networks;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import com.google.common.math.Stats;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

@AutoValue
public abstract class BSSID {

    @JsonProperty
    public abstract Map<String, SSID> ssids();

    @JsonProperty
    public abstract String oui();

    @JsonProperty
    public abstract String bssid();

    private DateTime lastSeen = new DateTime();

    private boolean isWPS;

    @JsonProperty("is_wps")
    public boolean isWPS() {
        return isWPS;
    }

    @JsonProperty("last_seen")
    public DateTime getLastSeen() {
        return lastSeen;
    }

    @JsonProperty("average_recent_signal_strength")
    public int averageRecentSignalStrength() {
        List<Integer> records = Lists.newArrayList();

        for (SSID ssid : ssids().values()) {
            for (Channel channel : ssid.channels().values()) {
                records.addAll(channel.signalStrengthTable().copyOfAllValues());
            }
        }

        if (records.isEmpty()) {
            return -100;
        }

        return (int) Math.round(Stats.meanOf(records));
    }

    public static BSSID create(Map<String, SSID> ssids, String oui, String bssid) {
        return builder()
                .ssids(ssids)
                .oui(oui)
                .bssid(bssid)
                .build();
    }

    @JsonIgnore
    public void updateLastSeen() {
        this.lastSeen = new DateTime();
    }

    @JsonIgnore
    public void updateIsWPS(boolean isWPS) {
        this.isWPS = isWPS;
    }

    public static Builder builder() {
        return new AutoValue_BSSID.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssids(Map<String, SSID> ssids);

        public abstract Builder oui(String oui);

        public abstract Builder bssid(String bssid);

        public abstract BSSID build();
    }

}