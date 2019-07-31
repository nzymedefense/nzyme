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

package horse.wtf.nzyme.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateSessionRequest {

    @JsonProperty
    @NotEmpty
    public abstract String username();

    @JsonProperty
    @NotEmpty
    public abstract String password();

    @JsonCreator
    public static CreateSessionRequest create(@JsonProperty("username") @NotEmpty String username, @JsonProperty("password") @NotEmpty String password) {
        return builder()
                .username(username)
                .password(password)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateSessionRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder username(String username);

        public abstract Builder password(String password);

        public abstract CreateSessionRequest build();
    }
}
