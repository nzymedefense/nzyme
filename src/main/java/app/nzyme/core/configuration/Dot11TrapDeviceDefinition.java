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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

import java.util.List;

@AutoValue
public abstract class Dot11TrapDeviceDefinition {

    public abstract String device();
    public abstract List<Integer> channels();
    public abstract String channelHopCommand();
    public abstract int channelHopInterval();
    public abstract boolean skipEnableMonitor();
    public abstract Dot11TrapConfiguration trap();

    public static Dot11TrapDeviceDefinition create(String device, List<Integer> channels, String channelHopCommand, int channelHopInterval, boolean skipEnableMonitor, Dot11TrapConfiguration trap) {
        return builder()
                .device(device)
                .channels(channels)
                .channelHopCommand(channelHopCommand)
                .channelHopInterval(channelHopInterval)
                .skipEnableMonitor(skipEnableMonitor)
                .trap(trap)
                .build();
    }


    @JsonIgnore
    public static boolean checkConfig(Config c) {
        return !Strings.isNullOrEmpty(c.getString(ConfigurationKeys.DEVICE))
                && !Strings.isNullOrEmpty(c.getString(ConfigurationKeys.HOP_COMMAND))
                && c.getInt(ConfigurationKeys.HOP_INTERVAL) >= 0
                && c.getIntList(ConfigurationKeys.CHANNELS) != null && !c.getIntList(ConfigurationKeys.CHANNELS).isEmpty()
                && c.getConfig(ConfigurationKeys.TRAP) != null;
    }

    public static Builder builder() {
        return new AutoValue_Dot11TrapDeviceDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder device(String device);

        public abstract Builder channels(List<Integer> channels);

        public abstract Builder channelHopCommand(String channelHopCommand);

        public abstract Builder channelHopInterval(int channelHopInterval);

        public abstract Builder trap(Dot11TrapConfiguration trap);

        public abstract Builder skipEnableMonitor(boolean skipEnableMonitor);

        public abstract Dot11TrapDeviceDefinition build();
    }

}
