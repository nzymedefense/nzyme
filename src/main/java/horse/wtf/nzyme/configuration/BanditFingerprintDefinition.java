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

@AutoValue
public abstract class BanditFingerprintDefinition {

    public abstract String fingerprint();
    public abstract String name();

    public static BanditFingerprintDefinition create(String fingerprint, String name) {
        return builder()
                .fingerprint(fingerprint)
                .name(name)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BanditFingerprintDefinition.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder fingerprint(String fingerprint);

        public abstract Builder name(String name);

        public abstract BanditFingerprintDefinition build();
    }

}
