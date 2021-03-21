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

package horse.wtf.nzyme.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

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

    @JsonProperty("is_active")
    public abstract boolean isActive();

    @JsonProperty("channels")
    public abstract List<Integer> channels();

    @JsonProperty("current_channel")
    public abstract int currentChannel();

    @JsonProperty("total_frames")
    public abstract long totalFrames();

    @JsonProperty("raises_alerts")
    public abstract List<String> raisesAlerts();

    public static ProbeResponse create(String name, String className, String networkInterfaceName, boolean isInLoop, boolean isActive, List<Integer> channels, int currentChannel, long totalFrames, List<String> raisesAlerts) {
        return builder()
                .name(name)
                .className(className)
                .networkInterfaceName(networkInterfaceName)
                .isInLoop(isInLoop)
                .isActive(isActive)
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

        public abstract Builder isActive(boolean isActive);

        public abstract Builder channels(List<Integer> channels);

        public abstract Builder currentChannel(int currentChannel);

        public abstract Builder totalFrames(long totalFrames);

        public abstract Builder raisesAlerts(List<String> raisesAlerts);

        public abstract ProbeResponse build();
    }
}
