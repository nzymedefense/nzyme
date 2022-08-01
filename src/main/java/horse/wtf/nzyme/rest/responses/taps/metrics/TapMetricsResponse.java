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

package horse.wtf.nzyme.rest.responses.taps.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class TapMetricsResponse {

    @JsonProperty("gauges")
    public abstract Map<String, TapMetricsGaugeResponse> gauges();

    public static TapMetricsResponse create(Map<String, TapMetricsGaugeResponse> gauges) {
        return builder()
                .gauges(gauges)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMetricsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder gauges(Map<String, TapMetricsGaugeResponse> gauges);

        public abstract TapMetricsResponse build();
    }

}
