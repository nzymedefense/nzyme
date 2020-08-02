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

package horse.wtf.nzyme.configuration.base;

import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.Role;

import javax.annotation.Nullable;

@AutoValue
public abstract class BaseConfiguration {

    public abstract String nodeId();
    public abstract Role mode();
    public abstract String dataDirectory();
    public abstract Boolean anonymize();

    public static BaseConfiguration create(String nodeId, Role mode, String dataDirectory, Boolean anonymize) {
        return builder()
                .nodeId(nodeId)
                .mode(mode)
                .dataDirectory(dataDirectory)
                .anonymize(anonymize)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BaseConfiguration.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder nodeId(String nodeId);

        public abstract Builder mode(Role mode);

        public abstract Builder dataDirectory(String dataDirectory);

        public abstract Builder anonymize(Boolean anonymize);

        public abstract BaseConfiguration build();
    }

}