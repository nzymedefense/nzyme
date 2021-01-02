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

package horse.wtf.nzyme.configuration;

import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;

@AutoValue
public abstract class Dot11MonitorDefinition {

    public abstract String device();
    public abstract ImmutableList<Integer> channels();
    public abstract String channelHopCommand();
    public abstract Integer channelHopInterval();

    public static Dot11MonitorDefinition create(String device, ImmutableList<Integer> channels, String channelHopCommand, Integer channelHopInterval) {
        return builder()
                .device(device)
                .channels(channels)
                .channelHopCommand(channelHopCommand)
                .channelHopInterval(channelHopInterval)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11MonitorDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder device(String device);

        public abstract Builder channels(ImmutableList<Integer> channels);

        public abstract Builder channelHopCommand(String channelHopCommand);

        public abstract Builder channelHopInterval(Integer channelHopInterval);

        public abstract Dot11MonitorDefinition build();
    }

    public static boolean checkConfig(Config c) {
        return !Strings.isNullOrEmpty(c.getString(ConfigurationKeys.DEVICE))
                && c.getIntList(ConfigurationKeys.CHANNELS) != null && !c.getIntList(ConfigurationKeys.CHANNELS).isEmpty()
                && !Strings.isNullOrEmpty(c.getString(ConfigurationKeys.HOP_COMMAND))
                && c.getInt(ConfigurationKeys.HOP_INTERVAL) >= 0;
    }

}
