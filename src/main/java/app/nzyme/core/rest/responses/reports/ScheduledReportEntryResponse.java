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

package app.nzyme.core.rest.responses.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

import java.util.List;

@AutoValue
public abstract class ScheduledReportEntryResponse {

    @JsonProperty("id")
    public abstract String id();

    @JsonProperty("name")
    public abstract String name();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    @JsonProperty("next_fire_time")
    public abstract DateTime nextFireTime();

    @JsonProperty("previous_fire_time")
    public abstract DateTime previousFireTime();

    @JsonProperty("trigger_state")
    public abstract String triggerState();

    @JsonProperty("cron_expression")
    public abstract String cronExpression();

    @JsonProperty("schedule_string")
    public abstract String scheduleString();

    @JsonProperty("email_receivers")
    public abstract List<String> emailReceivers();

    @JsonProperty("recent_execution_log")
    public abstract List<ExecutionLogEntryResponse> recentExecutionLog();

    public static ScheduledReportEntryResponse create(String id, String name, DateTime createdAt, DateTime nextFireTime, DateTime previousFireTime, String triggerState, String cronExpression, String scheduleString, List<String> emailReceivers, List<ExecutionLogEntryResponse> recentExecutionLog) {
        return builder()
                .id(id)
                .name(name)
                .createdAt(createdAt)
                .nextFireTime(nextFireTime)
                .previousFireTime(previousFireTime)
                .triggerState(triggerState)
                .cronExpression(cronExpression)
                .scheduleString(scheduleString)
                .emailReceivers(emailReceivers)
                .recentExecutionLog(recentExecutionLog)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ScheduledReportEntryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder name(String name);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder nextFireTime(DateTime nextFireTime);

        public abstract Builder previousFireTime(DateTime previousFireTime);

        public abstract Builder triggerState(String triggerState);

        public abstract Builder cronExpression(String cronExpression);

        public abstract Builder scheduleString(String scheduleString);

        public abstract Builder emailReceivers(List<String> emailReceivers);

        public abstract Builder recentExecutionLog(List<ExecutionLogEntryResponse> recentExecutionLog);

        public abstract ScheduledReportEntryResponse build();
    }
}
