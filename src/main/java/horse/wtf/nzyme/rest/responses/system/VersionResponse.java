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

@AutoValue
public abstract class VersionResponse {

    @JsonProperty
    public abstract String version();

    @JsonProperty("new_version_available")
    public abstract boolean newVersionAvailable();

    @JsonProperty("checks_version")
    public abstract boolean checksVersion();

    public static VersionResponse create(String version, boolean newVersionAvailable, boolean checksVersion) {
        return builder()
                .version(version)
                .newVersionAvailable(newVersionAvailable)
                .checksVersion(checksVersion)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_VersionResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder version(String version);

        public abstract Builder newVersionAvailable(boolean newVersionAvailable);

        public abstract Builder checksVersion(boolean checksVersion);

        public abstract VersionResponse build();
    }

}
