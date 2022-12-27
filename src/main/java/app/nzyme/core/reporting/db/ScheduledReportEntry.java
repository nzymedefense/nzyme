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

package app.nzyme.core.reporting.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ScheduledReportEntry {

    public abstract String name();
    public abstract DateTime createdAt();
    public abstract DateTime nextFireTime();
    public abstract DateTime previousFireTime();
    public abstract String triggerState();
    public abstract String cronExpression();

    public static ScheduledReportEntry create(String name, DateTime createdAt, DateTime nextFireTime, DateTime previousFireTime, String triggerState, String cronExpression) {
        return builder()
                .name(name)
                .createdAt(createdAt)
                .nextFireTime(nextFireTime)
                .previousFireTime(previousFireTime)
                .triggerState(triggerState)
                .cronExpression(cronExpression)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ScheduledReportEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract Builder nextFireTime(DateTime nextFireTime);

        public abstract Builder previousFireTime(DateTime previousFireTime);

        public abstract Builder triggerState(String triggerState);

        public abstract Builder cronExpression(String cronExpression);

        public abstract ScheduledReportEntry build();
    }

}
