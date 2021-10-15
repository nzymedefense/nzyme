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

package horse.wtf.nzyme.rest.responses.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ScheduledReportsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("reports")
    public abstract List<ScheduledReportEntryResponse> reports();

    public static ScheduledReportsListResponse create(long total, List<ScheduledReportEntryResponse> reports) {
        return builder()
                .total(total)
                .reports(reports)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ScheduledReportsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder reports(List<ScheduledReportEntryResponse> reports);

        public abstract ScheduledReportsListResponse build();
    }

}
