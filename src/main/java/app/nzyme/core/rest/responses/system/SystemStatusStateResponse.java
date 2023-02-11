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

package app.nzyme.core.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.systemstatus.SystemStatus;

@AutoValue
public abstract class SystemStatusStateResponse {

    @JsonProperty
    public abstract SystemStatus.TYPE name();

    @JsonProperty
    public abstract boolean active();

    public static SystemStatusStateResponse create(SystemStatus.TYPE name, boolean active) {
        return builder()
                .name(name)
                .active(active)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_SystemStatusStateResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(SystemStatus.TYPE name);

        public abstract Builder active(boolean active);

        public abstract SystemStatusStateResponse build();
    }

}
