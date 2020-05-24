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

package horse.wtf.nzyme.configuration.tracker;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.Role;
import horse.wtf.nzyme.configuration.TrackerDeviceConfiguration;

@AutoValue
public abstract class TrackerConfiguration {

    public abstract Role role();
    public abstract String nzymeId();

    public abstract TrackerDeviceConfiguration trackerDevice();

    public static TrackerConfiguration create(Role role, String nzymeId, TrackerDeviceConfiguration trackerDevice) {
        return builder()
                .role(role)
                .nzymeId(nzymeId)
                .trackerDevice(trackerDevice)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TrackerConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder role(Role role);

        public abstract Builder nzymeId(String nzymeId);

        public abstract Builder trackerDevice(TrackerDeviceConfiguration trackerDevice);

        public abstract TrackerConfiguration build();
    }

}
