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

package app.nzyme.core.configuration.base;

import com.google.auto.value.AutoValue;
import app.nzyme.core.Role;

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