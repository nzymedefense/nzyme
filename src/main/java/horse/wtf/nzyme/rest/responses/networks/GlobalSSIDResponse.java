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

import java.util.List;

@AutoValue
public abstract class GlobalSSIDResponse {

    @JsonProperty("human_readable")
    public abstract boolean humanReadable();

    @JsonProperty
    public abstract String name();

    @JsonProperty("is_monitored")
    public abstract boolean isMonitored();

    @JsonProperty
    public abstract List<SSIDSecurityResponse> security();

    @JsonProperty("total_frames")
    public abstract long totalFrames();

    @JsonProperty("bssids")
    public abstract List<String> bssids();

    public static GlobalSSIDResponse create(boolean humanReadable, String name, boolean isMonitored, List<SSIDSecurityResponse> security, long totalFrames, List<String> bssids) {
        return builder()
                .humanReadable(humanReadable)
                .name(name)
                .isMonitored(isMonitored)
                .security(security)
                .totalFrames(totalFrames)
                .bssids(bssids)
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

        public abstract GlobalSSIDResponse build();
    }

}
