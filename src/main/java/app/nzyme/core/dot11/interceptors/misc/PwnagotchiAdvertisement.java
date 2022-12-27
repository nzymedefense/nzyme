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

package app.nzyme.core.dot11.interceptors.misc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class PwnagotchiAdvertisement {

    @Nullable
    public abstract String name();

    @Nullable
    public abstract String version();

    @Nullable
    public abstract String identity();

    @Nullable
    public abstract Double uptime();

    @Nullable
    public abstract Integer pwndThisRun();

    @Nullable
    public abstract Integer pwndTotal();

    @JsonCreator
    public static PwnagotchiAdvertisement create(@JsonProperty("name") String name,
                                                 @JsonProperty("version") String version,
                                                 @JsonProperty("identity") String identity,
                                                 @JsonProperty("uptime") Double uptime,
                                                 @JsonProperty("pwnd_run") Integer pwndThisRun,
                                                 @JsonProperty("pwnd_tot") Integer pwndTotal) {
        return builder()
                .name(name)
                .version(version)
                .identity(identity)
                .uptime(uptime)
                .pwndThisRun(pwndThisRun)
                .pwndTotal(pwndTotal)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_PwnagotchiAdvertisement.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder version(String version);

        public abstract Builder identity(String identity);

        public abstract Builder uptime(Double uptime);

        public abstract Builder pwndThisRun(Integer pwndThisRun);

        public abstract Builder pwndTotal(Integer pwndTotal);

        public abstract PwnagotchiAdvertisement build();
    }

}
