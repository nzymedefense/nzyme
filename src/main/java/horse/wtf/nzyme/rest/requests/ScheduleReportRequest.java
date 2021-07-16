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

package horse.wtf.nzyme.rest.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class ScheduleReportRequest {

    @JsonProperty("report_type")
    public abstract String reportType();

    @JsonProperty("hour_of_day")
    public abstract int hourOfDay();

    @JsonProperty("minute_of_hour")
    public abstract int minuteOfHour();

    public static ScheduleReportRequest create(String reportType, int hourOfDay, int minuteOfHour) {
        return builder()
                .reportType(reportType)
                .hourOfDay(hourOfDay)
                .minuteOfHour(minuteOfHour)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ScheduleReportRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder reportType(String reportType);

        public abstract Builder hourOfDay(int hourOfDay);

        public abstract Builder minuteOfHour(int minuteOfHour);

        public abstract ScheduleReportRequest build();
    }

}