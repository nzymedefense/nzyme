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

    @JsonProperty("expected_signal_strength")
    @Nullable
    public abstract ExpectedSignalStrengthResponse expectedSignalStrength();

    @JsonProperty("is_monitored")
    public abstract boolean isMonitored();

    public static SSIDResponse create(List<SSIDSecurityResponse> security, String bssid, boolean humanReadable, String name, Map<Integer, ChannelResponse> channels, List<String> fingerprints, BeaconRate beaconRate, List<AverageBeaconRate> beaconRateHistory, Integer beaconRateThreshold, ExpectedSignalStrengthResponse expectedSignalStrength, boolean isMonitored) {
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
                .expectedSignalStrength(expectedSignalStrength)
                .isMonitored(isMonitored)
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

        public abstract Builder expectedSignalStrength(ExpectedSignalStrengthResponse expectedSignalStrength);

        public abstract Builder isMonitored(boolean isMonitored);

        public abstract SSIDResponse build();
    }

}
