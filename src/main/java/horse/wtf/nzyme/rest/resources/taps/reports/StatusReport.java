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

package horse.wtf.nzyme.rest.resources.taps.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class StatusReport {

    public abstract String tapName();
    public abstract DateTime timestamp();
    public abstract Long processedBytesTotal();
    public abstract Long processedBytesAverage();
    public abstract SystemMetrics systemMetrics();

    @JsonCreator
    public static StatusReport create(@JsonProperty("tap_name") String tapName,
                                      @JsonProperty("timestamp") DateTime timestamp,
                                      @JsonProperty("processed_bytes_total") Long processedBytesTotal,
                                      @JsonProperty("processed_bytes_average") long processedBytesAverage,
                                      @JsonProperty("system_metrics") SystemMetrics systemMetrics) {
        return builder()
                .tapName(tapName)
                .timestamp(timestamp)
                .processedBytesTotal(processedBytesTotal)
                .processedBytesAverage(processedBytesAverage)
                .systemMetrics(systemMetrics)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_StatusReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapName(String tapName);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder processedBytesTotal(Long processedBytesTotal);

        public abstract Builder processedBytesAverage(Long processedBytesAverage);

        public abstract Builder systemMetrics(SystemMetrics systemMetrics);

        public abstract StatusReport build();
    }

}
