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

package horse.wtf.nzyme.configuration.tracker;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.bandits.trackers.hid.TrackerHID;
import horse.wtf.nzyme.configuration.Dot11MonitorDefinition;
import horse.wtf.nzyme.configuration.UplinkDeviceConfiguration;

import java.util.List;

@AutoValue
public abstract class TrackerConfiguration {

    public abstract Role role();

    public abstract UplinkDeviceConfiguration uplinkDevice();
    public abstract List<TrackerHID.TYPE> hids();

    public abstract List<Dot11MonitorDefinition> dot11Monitors();

    public static TrackerConfiguration create(Role role, UplinkDeviceConfiguration uplinkDevice, List<TrackerHID.TYPE> hids, List<Dot11MonitorDefinition> dot11Monitors) {
        return builder()
                .role(role)
                .uplinkDevice(uplinkDevice)
                .hids(hids)
                .dot11Monitors(dot11Monitors)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrackerConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder role(Role role);

        public abstract Builder uplinkDevice(UplinkDeviceConfiguration uplinkDevice);

        public abstract Builder hids(List<TrackerHID.TYPE> hids);

        public abstract Builder dot11Monitors(List<Dot11MonitorDefinition> dot11Monitors);

        public abstract TrackerConfiguration build();
    }

}
