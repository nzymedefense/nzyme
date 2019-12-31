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

package horse.wtf.nzyme.rest.responses.bandits;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import horse.wtf.nzyme.bandits.identifiers.BanditIdentifier;

import java.util.Map;

@AutoValue
public abstract class BanditIdentifierResponse {

    @JsonProperty
    public abstract Map<String, Object> configuration();

    @JsonProperty
    public abstract BanditIdentifier.TYPE type();

    @JsonProperty
    public abstract String description();

    @JsonProperty
    public abstract String matches();

    public static BanditIdentifierResponse create(Map<String, Object> configuration, BanditIdentifier.TYPE type, String description, String matches) {
        return builder()
                .configuration(configuration)
                .type(type)
                .description(description)
                .matches(matches)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BanditIdentifierResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder configuration(Map<String, Object> configuration);

        public abstract Builder type(BanditIdentifier.TYPE type);

        public abstract Builder description(String description);

        public abstract Builder matches(String matches);

        public abstract BanditIdentifierResponse build();
    }

}
