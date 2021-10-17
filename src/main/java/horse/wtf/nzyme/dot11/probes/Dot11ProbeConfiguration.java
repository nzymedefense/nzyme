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

package horse.wtf.nzyme.dot11.probes;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import horse.wtf.nzyme.configuration.Dot11NetworkDefinition;
import horse.wtf.nzyme.configuration.Dot11TrapDeviceDefinition;
import horse.wtf.nzyme.notifications.Uplink;

import javax.annotation.Nullable;

@AutoValue
public abstract class Dot11ProbeConfiguration {

    public abstract String probeName();

    @Nullable
    public abstract ImmutableList<Uplink> uplinks();
    public abstract String nzymeId();

    public abstract String networkInterfaceName();
    public abstract ImmutableList<Integer> channels();
    public abstract Integer channelHopInterval();
    public abstract String channelHopCommand();
    public abstract boolean skipEnableMonitor();
    public abstract int maxIdleTimeSeconds();

    @Nullable
    public abstract ImmutableList<Dot11NetworkDefinition> getDot11Networks();

    @Nullable
    public abstract ImmutableList<Dot11TrapDeviceDefinition> getDot11TrapDevices();

    public static Dot11ProbeConfiguration create(String probeName, ImmutableList<Uplink> uplinks, String nzymeId, String networkInterfaceName, ImmutableList<Integer> channels, Integer channelHopInterval, String channelHopCommand, boolean skipEnableMonitor, int maxIdleTimeSeconds, ImmutableList<Dot11NetworkDefinition> getDot11Networks, ImmutableList<Dot11TrapDeviceDefinition> getDot11TrapDevices) {
        return builder()
                .probeName(probeName)
                .uplinks(uplinks)
                .nzymeId(nzymeId)
                .networkInterfaceName(networkInterfaceName)
                .channels(channels)
                .channelHopInterval(channelHopInterval)
                .channelHopCommand(channelHopCommand)
                .skipEnableMonitor(skipEnableMonitor)
                .maxIdleTimeSeconds(maxIdleTimeSeconds)
                .getDot11Networks(getDot11Networks)
                .getDot11TrapDevices(getDot11TrapDevices)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11ProbeConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder probeName(String probeName);

        public abstract Builder uplinks(ImmutableList<Uplink> uplinks);

        public abstract Builder nzymeId(String nzymeId);

        public abstract Builder networkInterfaceName(String networkInterfaceName);

        public abstract Builder channels(ImmutableList<Integer> channels);

        public abstract Builder channelHopInterval(Integer channelHopInterval);

        public abstract Builder channelHopCommand(String channelHopCommand);

        public abstract Builder skipEnableMonitor(boolean skipEnableMonitor);

        public abstract Builder maxIdleTimeSeconds(int maxIdleTimeSeconds);

        public abstract Builder getDot11Networks(ImmutableList<Dot11NetworkDefinition> getDot11Networks);

        public abstract Builder getDot11TrapDevices(ImmutableList<Dot11TrapDeviceDefinition> getDot11TrapDevices);

        public abstract Dot11ProbeConfiguration build();
    }

}