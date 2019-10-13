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
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class BSSIDResponse {

    @JsonProperty("bssid")
    public abstract String bssid();

    @JsonProperty("signal_strength")
    public abstract int signalStrength();

    @JsonProperty("ssids")
    public abstract List<String> ssids();

    @JsonProperty("oui")
    public abstract String oui();

    @JsonProperty("security_mechanisms")
    public abstract List<String> securityMechanisms();

    @JsonProperty("fingerprint_count")
    public abstract int fingerprintCount();

    @JsonProperty("has_wps")
    public abstract boolean hasWPS();

    @JsonProperty("last_seen")
    public abstract DateTime lastSeen();

    public static BSSIDResponse create(String bssid, int signalStrength, List<String> ssids, String oui, List<String> securityMechanisms, int fingerprintCount, boolean hasWPS, DateTime lastSeen) {
        return builder()
                .bssid(bssid)
                .signalStrength(signalStrength)
                .ssids(ssids)
                .oui(oui)
                .securityMechanisms(securityMechanisms)
                .fingerprintCount(fingerprintCount)
                .hasWPS(hasWPS)
                .lastSeen(lastSeen)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder bssid(String bssid);

        public abstract Builder signalStrength(int signalStrength);

        public abstract Builder ssids(List<String> ssids);

        public abstract Builder oui(String oui);

        public abstract Builder securityMechanisms(List<String> securityMechanisms);

        public abstract Builder fingerprintCount(int fingerprintCount);

        public abstract Builder hasWPS(boolean hasWPS);

        public abstract Builder lastSeen(DateTime lastSeen);

        public abstract BSSIDResponse build();
    }

}
