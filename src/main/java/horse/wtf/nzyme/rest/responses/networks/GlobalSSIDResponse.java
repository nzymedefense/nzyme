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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class GlobalSSIDResponse {

    @JsonProperty("human_readable")
    public abstract boolean humanReadable();

    @JsonProperty
    public abstract String name();

    @JsonProperty("is_monitored")
    public abstract boolean isMonitored();

    @JsonProperty("security")
    public abstract List<SSIDSecurityResponse> security();

    @JsonProperty("total_frames")
    public abstract long totalFrames();

    @JsonProperty("bssids")
    public abstract List<String> bssids();

    @JsonProperty("beacon_rates")
    public abstract Map<String, List<AverageBeaconRate>> beaconRates();

    @JsonProperty("beacon_rate_threshold")
    @Nullable
    public abstract Integer beaconRateThreshold();

    public static GlobalSSIDResponse create(boolean humanReadable, String name, boolean isMonitored, List<SSIDSecurityResponse> security, long totalFrames, List<String> bssids, Map<String, List<AverageBeaconRate>> beaconRates, Integer beaconRateThreshold) {
        return builder()
                .humanReadable(humanReadable)
                .name(name)
                .isMonitored(isMonitored)
                .security(security)
                .totalFrames(totalFrames)
                .bssids(bssids)
                .beaconRates(beaconRates)
                .beaconRateThreshold(beaconRateThreshold)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GlobalSSIDResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder humanReadable(boolean humanReadable);

        public abstract Builder name(String name);

        public abstract Builder isMonitored(boolean isMonitored);

        public abstract Builder security(List<SSIDSecurityResponse> security);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder bssids(List<String> bssids);

        public abstract Builder beaconRates(Map<String, List<AverageBeaconRate>> beaconRates);

        public abstract Builder beaconRateThreshold(Integer beaconRateThreshold);

        public abstract GlobalSSIDResponse build();
    }

}
