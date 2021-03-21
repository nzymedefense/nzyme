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

package horse.wtf.nzyme.rest.responses.networks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.dot11.networks.beaconrate.AverageBeaconRate;
import horse.wtf.nzyme.dot11.networks.beaconrate.BeaconRate;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class SSIDResponse {

    @JsonProperty
    public abstract List<SSIDSecurityResponse> security();

    @JsonProperty
    public abstract String bssid();

    @JsonProperty("human_readable")
    public abstract boolean humanReadable();

    @JsonProperty
    public abstract String name();

    @JsonProperty
    public abstract Map<Integer, ChannelResponse> channels();

    @JsonProperty
    public abstract List<String> fingerprints();

    @JsonProperty("beacon_rate")
    public abstract BeaconRate beaconRate();

    @JsonProperty("beacon_rate_history")
    @Nullable
    public abstract List<AverageBeaconRate> beaconRateHistory();

    @JsonProperty("beacon_rate_threshold")
    @Nullable
    public abstract Integer beaconRateThreshold();

    @JsonProperty("is_monitored")
    public abstract boolean isMonitored();

    @JsonProperty("most_active_channel")
    public abstract Integer mostActiveChannel();

    public static SSIDResponse create(List<SSIDSecurityResponse> security, String bssid, boolean humanReadable, String name, Map<Integer, ChannelResponse> channels, List<String> fingerprints, BeaconRate beaconRate, List<AverageBeaconRate> beaconRateHistory, Integer beaconRateThreshold, boolean isMonitored, Integer mostActiveChannel) {
        return builder()
                .security(security)
                .bssid(bssid)
                .humanReadable(humanReadable)
                .name(name)
                .channels(channels)
                .fingerprints(fingerprints)
                .beaconRate(beaconRate)
                .beaconRateHistory(beaconRateHistory)
                .beaconRateThreshold(beaconRateThreshold)
                .isMonitored(isMonitored)
                .mostActiveChannel(mostActiveChannel)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SSIDResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder security(List<SSIDSecurityResponse> security);

        public abstract Builder bssid(String bssid);

        public abstract Builder humanReadable(boolean humanReadable);

        public abstract Builder name(String name);

        public abstract Builder channels(Map<Integer, ChannelResponse> channels);

        public abstract Builder fingerprints(List<String> fingerprints);

        public abstract Builder beaconRate(BeaconRate beaconRate);

        public abstract Builder beaconRateHistory(List<AverageBeaconRate> beaconRateHistory);

        public abstract Builder beaconRateThreshold(Integer beaconRateThreshold);

        public abstract Builder isMonitored(boolean isMonitored);

        public abstract Builder mostActiveChannel(Integer mostActiveChannel);

        public abstract SSIDResponse build();
    }

}
