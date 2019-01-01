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

package horse.wtf.nzyme.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.systemstatus.SystemStatus;

@AutoValue
public abstract class SystemStatusStateResponse {

    @JsonProperty
    public abstract SystemStatus.TYPE name();

    @JsonProperty
    public abstract boolean active();

    public static SystemStatusStateResponse create(SystemStatus.TYPE name, boolean active) {
        return builder()
                .name(name)
                .active(active)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SystemStatusStateResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(SystemStatus.TYPE name);

        public abstract Builder active(boolean active);

        public abstract SystemStatusStateResponse build();
    }

}
