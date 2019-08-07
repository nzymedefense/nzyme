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

package horse.wtf.nzyme.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

import java.util.List;

@AutoValue
public abstract class Dot11TrapDeviceDefinition {

    public abstract String deviceSender();
    public abstract List<Integer> channels();
    public abstract String channelHopCommand();
    public abstract int channelHopInterval();
    public abstract List<Dot11TrapConfiguration> traps();

    @JsonIgnore
    public static boolean checkConfig(Config c) {
        return !Strings.isNullOrEmpty(c.getString(ConfigurationKeys.DEVICE_SENDER))
                && !Strings.isNullOrEmpty(c.getString(ConfigurationKeys.HOP_COMMAND))
                && c.getInt(ConfigurationKeys.HOP_INTERVAL) >= 0
                && c.getIntList(ConfigurationKeys.CHANNELS) != null && !c.getIntList(ConfigurationKeys.CHANNELS).isEmpty()
                && c.getConfigList(ConfigurationKeys.TRAPS) != null && !c.getConfigList(ConfigurationKeys.TRAPS).isEmpty();
    }


    public static Dot11TrapDeviceDefinition create(String deviceSender, List<Integer> channels, String channelHopCommand, int channelHopInterval, List<Dot11TrapConfiguration> traps) {
        return builder()
                .deviceSender(deviceSender)
                .channels(channels)
                .channelHopCommand(channelHopCommand)
                .channelHopInterval(channelHopInterval)
                .traps(traps)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11TrapDeviceDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder deviceSender(String deviceSender);

        public abstract Builder channels(List<Integer> channels);

        public abstract Builder channelHopCommand(String channelHopCommand);

        public abstract Builder channelHopInterval(int channelHopInterval);

        public abstract Builder traps(List<Dot11TrapConfiguration> traps);

        public abstract Dot11TrapDeviceDefinition build();
    }

}
