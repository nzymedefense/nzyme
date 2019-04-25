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
import org.joda.time.DateTime;

import java.util.Map;

@AutoValue
public abstract class BSSID {

    @JsonProperty
    public abstract Map<String, SSID> ssids();

    @JsonProperty
    public abstract String oui();

    @JsonProperty
    public abstract String bssid();

    @JsonProperty("last_seen")
    public DateTime lastSeen = new DateTime();

    @JsonProperty("is_wps")
    public boolean isWPS;

    @JsonProperty("best_recent_signal_quality")
    public int bestRecentSignalQuality() {
        int best = 0;

        for (SSID ssid : ssids().values()) {
            for (Channel channel : ssid.channels().values()) {
                if (channel.signalQualityRecentAverage() > best) {
                    best = channel.signalQualityRecentAverage();
                }
            }
        }


        return best;
    }

    @JsonProperty("fingerprinting_ok")
    public boolean fingerprintingOkay() {
        boolean result = true;

        for (SSID ssid : ssids().values()) {
            for (Channel channel : ssid.channels().values()) {
                if (channel.fingerprints().size() > 1) {
                    return false;
                }
            }

        }

        return result;
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