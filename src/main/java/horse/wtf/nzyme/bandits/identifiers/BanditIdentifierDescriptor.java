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

package horse.wtf.nzyme.bandits.identifiers;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class BanditIdentifierDescriptor {

    public abstract BanditIdentifier.TYPE type();

    public abstract String description();

    public abstract String matches();

    public static BanditIdentifierDescriptor create(BanditIdentifier.TYPE type, String description, String matches) {
        return builder()
                .type(type)
                .description(description)
                .matches(matches)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BanditIdentifierDescriptor.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder type(BanditIdentifier.TYPE type);

        public abstract Builder description(String description);

        public abstract Builder matches(String matches);

        public abstract BanditIdentifierDescriptor build();
    }
}
