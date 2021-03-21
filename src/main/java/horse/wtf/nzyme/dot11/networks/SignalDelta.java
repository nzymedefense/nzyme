/*
 * This file is part of nzyme.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */

package horse.wtf.nzyme.dot11.networks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SignalDelta {

    @JsonProperty
    public abstract int lower();

    @JsonProperty
    public abstract int upper();

    public static SignalDelta create(int lower, int upper) {
        if (upper > 100) {
            upper = 100;
        }

        return builder()
                .lower(lower)
                .upper(upper)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SignalDelta.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder lower(int lower);

        public abstract Builder upper(int upper);

        public abstract SignalDelta build();
    }

}
