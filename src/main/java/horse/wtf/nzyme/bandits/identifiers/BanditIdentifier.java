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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.dot11.frames.Dot11BeaconFrame;
import horse.wtf.nzyme.dot11.frames.Dot11DeauthenticationFrame;
import horse.wtf.nzyme.dot11.frames.Dot11ProbeResponseFrame;

import java.util.Map;
import java.util.Optional;

public interface BanditIdentifier {

    enum TYPE {
        FINGERPRINT,
        SSID,
        SIGNAL_STRENGTH
    }

    @JsonProperty
    Descriptor descriptor();

    @JsonProperty
    Map<String, Object> configuration();

    @JsonIgnore
    Optional<Boolean> matches(Dot11DeauthenticationFrame frame);

    @JsonIgnore
    Optional<Boolean> matches(Dot11BeaconFrame frame);

    @JsonIgnore
    Optional<Boolean> matches(Dot11ProbeResponseFrame frame);

    @AutoValue
    abstract class Descriptor {

        @JsonProperty
        public abstract TYPE type();

        @JsonProperty
        public abstract String description();

        @JsonProperty
        public abstract String matches();

        public static Descriptor create(TYPE type, String description, String matches) {
            return builder()
                    .type(type)
                    .description(description)
                    .matches(matches)
                    .build();
        }

        public static Builder builder() {
            return new AutoValue_BanditIdentifier_Descriptor.Builder();
        }

        @AutoValue.Builder
        public abstract static class Builder {
            public abstract Builder type(TYPE type);

            public abstract Builder description(String description);

            public abstract Builder matches(String matches);

            public abstract Descriptor build();
        }

    }

}
