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

package horse.wtf.nzyme.dot11.frames;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.dot11.Dot11MetaInformation;

import javax.annotation.Nullable;

@AutoValue
public abstract class Dot11ProbeResponseFrame {

    @Nullable
    public abstract String ssid();
    public abstract String destination();
    public abstract String transmitter();
    public abstract Dot11MetaInformation meta();

    public static Dot11ProbeResponseFrame create(String ssid, String destination, String transmitter, Dot11MetaInformation meta) {
        return builder()
                .ssid(ssid)
                .destination(destination)
                .transmitter(transmitter)
                .meta(meta)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11ProbeResponseFrame.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder destination(String destination);

        public abstract Builder transmitter(String transmitter);

        public abstract Builder meta(Dot11MetaInformation meta);

        public abstract Dot11ProbeResponseFrame build();
    }

}
