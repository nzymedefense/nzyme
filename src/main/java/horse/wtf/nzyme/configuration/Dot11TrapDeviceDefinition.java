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

    public abstract String device();
    public abstract List<Integer> channels();
    public abstract String channelHopCommand();
    public abstract int channelHopInterval();
    public abstract Dot11TrapConfiguration trap();

    public static Dot11TrapDeviceDefinition create(String device, List<Integer> channels, String channelHopCommand, int channelHopInterval, Dot11TrapConfiguration trap) {
        return builder()
                .device(device)
                .channels(channels)
                .channelHopCommand(channelHopCommand)
                .channelHopInterval(channelHopInterval)
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

        public abstract Dot11TrapDeviceDefinition build();
    }

}
