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

package horse.wtf.nzyme.dot11.anonymization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class Anonfile {

    @JsonProperty("ssids")
    public abstract Map<String,String> ssids();

    @JsonProperty("bssids")
    public abstract Map<String,String> bssids();

    @JsonCreator
    public static Anonfile create(@JsonProperty("ssids") Map<String, String> ssids, @JsonProperty("bssids") Map<String, String> bssids) {
        return builder()
                .ssids(ssids)
                .bssids(bssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Anonfile.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssids(Map<String, String> ssids);

        public abstract Builder bssids(Map<String, String> bssids);

        public abstract Anonfile build();
    }

}
