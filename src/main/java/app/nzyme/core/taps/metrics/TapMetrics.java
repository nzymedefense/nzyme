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

package app.nzyme.core.taps.metrics;

import com.google.auto.value.AutoValue;

import java.util.List;
import java.util.UUID;

@AutoValue
public abstract class TapMetrics {

    public abstract UUID tapUUID();
    public abstract List<TapMetricsGauge> gauges();

    public static TapMetrics create(UUID tapUUID, List<TapMetricsGauge> gauges) {
        return builder()
                .tapUUID(tapUUID)
                .gauges(gauges)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TapMetrics.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapUUID(UUID tapUUID);

        public abstract Builder gauges(List<TapMetricsGauge> gauges);

        public abstract TapMetrics build();
    }
}
