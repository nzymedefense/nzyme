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

import com.google.auto.value.AutoValue;
import com.google.common.base.Strings;
import com.typesafe.config.Config;

import java.util.List;

@AutoValue
public abstract class Dot11MonitorDefinition {

    public abstract String device();
    public abstract List<Integer> channels();
    public abstract String channelHopCommand();
    public abstract Integer channelHopInterval();

    public static Dot11MonitorDefinition create(String device, List<Integer> channels, String channelHopCommand, Integer channelHopInterval) {
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

        public abstract Builder channels(List<Integer> channels);

        public abstract Builder channelHopCommand(String channelHopCommand);

        public abstract Builder channelHopInterval(Integer channelHopInterval);

        public abstract Dot11MonitorDefinition build();
    }

    public static boolean checkConfig(Config c) {
        return !Strings.isNullOrEmpty(c.getString(Keys.DEVICE))
                && c.getIntList(Keys.CHANNELS) != null && !c.getIntList(Keys.CHANNELS).isEmpty()
                && !Strings.isNullOrEmpty(c.getString(Keys.HOP_COMMAND))
                && c.getInt(Keys.HOP_INTERVAL) >= 0;
    }

}
