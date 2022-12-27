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

package app.nzyme.core.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.alerts.Alert;

import java.util.List;

@AutoValue
public abstract class AlertConfigurationResponse {

    @JsonProperty
    public abstract List<Alert.TYPE_WIDE> enabled();

    @JsonProperty
    public abstract List<Alert.TYPE_WIDE> disabled();

    public static AlertConfigurationResponse create(List<Alert.TYPE_WIDE> enabled, List<Alert.TYPE_WIDE> disabled) {
        return builder()
                .enabled(enabled)
                .disabled(disabled)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AlertConfigurationResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder enabled(List<Alert.TYPE_WIDE> enabled);

        public abstract Builder disabled(List<Alert.TYPE_WIDE> disabled);

        public abstract AlertConfigurationResponse build();
    }
}
