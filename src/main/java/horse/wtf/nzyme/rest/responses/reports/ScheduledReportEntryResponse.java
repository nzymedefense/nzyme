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
import org.joda.time.DateTime;

import java.util.UUID;

@AutoValue
public abstract class ScheduledReportEntryResponse {

    @JsonProperty("id")
    public abstract String id();

    @JsonProperty("name")
    public abstract String name();

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

    public static ScheduledReportEntryResponse create(String id, String name, DateTime nextFireTime, DateTime previousFireTime, String triggerState, String cronExpression, String scheduleString) {
        return builder()
                .id(id)
                .name(name)
                .nextFireTime(nextFireTime)
                .previousFireTime(previousFireTime)
                .triggerState(triggerState)
                .cronExpression(cronExpression)
                .scheduleString(scheduleString)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ScheduledReportEntryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder id(String id);

        public abstract Builder name(String name);

        public abstract Builder nextFireTime(DateTime nextFireTime);

        public abstract Builder previousFireTime(DateTime previousFireTime);

        public abstract Builder triggerState(String triggerState);

        public abstract Builder cronExpression(String cronExpression);

        public abstract Builder scheduleString(String scheduleString);

        public abstract ScheduledReportEntryResponse build();
    }

}
