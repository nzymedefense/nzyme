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

package horse.wtf.nzyme.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ProbeResponse {

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("class_name")
    public abstract String className();

    @JsonProperty("network_interface")
    public abstract String networkInterfaceName();

    @JsonProperty("is_in_loop")
    public abstract boolean isInLoop();

    @JsonProperty("channels")
    public abstract List<Integer> channels();

    @JsonProperty("current_channel")
    public abstract int currentChannel();

    @JsonProperty("total_frames")
    public abstract long totalFrames();

    @JsonProperty("raises_alerts")
    public abstract List<String> raisesAlerts();

    public static ProbeResponse create(String name, String className, String networkInterfaceName, boolean isInLoop, List<Integer> channels, int currentChannel, long totalFrames, List<String> raisesAlerts) {
        return builder()
                .name(name)
                .className(className)
                .networkInterfaceName(networkInterfaceName)
                .isInLoop(isInLoop)
                .channels(channels)
                .currentChannel(currentChannel)
                .totalFrames(totalFrames)
                .raisesAlerts(raisesAlerts)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ProbeResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder className(String className);

        public abstract Builder networkInterfaceName(String networkInterfaceName);

        public abstract Builder isInLoop(boolean isInLoop);

        public abstract Builder channels(List<Integer> channels);

        public abstract Builder currentChannel(int currentChannel);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder raisesAlerts(List<String> raisesAlerts);

        public abstract ProbeResponse build();
    }
}
