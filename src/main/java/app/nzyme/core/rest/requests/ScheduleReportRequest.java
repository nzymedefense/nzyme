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

package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ScheduleReportRequest {

    public abstract String reportType();
    public abstract int hourOfDay();
    public abstract int minuteOfHour();
    public abstract List<String> emailReceivers();

    @JsonCreator
    public static ScheduleReportRequest create(@JsonProperty("report_type") String reportType, @JsonProperty("hour_of_day") int hourOfDay, @JsonProperty("minute_of_hour") int minuteOfHour, @JsonProperty("email_receivers") List<String> emailReceivers) {
        return builder()
                .reportType(reportType)
                .hourOfDay(hourOfDay)
                .minuteOfHour(minuteOfHour)
                .emailReceivers(emailReceivers)
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

        public abstract Builder emailReceivers(List<String> emailReceivers);

        public abstract ScheduleReportRequest build();
    }

}