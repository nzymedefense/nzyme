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

package horse.wtf.nzyme.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class AlertsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("alerts")
    public abstract List<AlertDetailsResponse> alerts();

    public static AlertsListResponse create(long total, List<AlertDetailsResponse> alerts) {
        return builder()
                .total(total)
                .alerts(alerts)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AlertsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder alerts(List<AlertDetailsResponse> alerts);

        public abstract AlertsListResponse build();
    }

}
