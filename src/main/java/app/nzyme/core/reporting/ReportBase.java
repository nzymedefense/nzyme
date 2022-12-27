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

package app.nzyme.core.reporting;

import org.quartz.Job;
import org.quartz.ScheduleBuilder;
import org.quartz.Trigger;

import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute;

public abstract class ReportBase {

    public enum EXECUTION_RESULT {
        SUCCESS,
        ERROR
    }

    protected final ScheduleBuilder<? extends Trigger> schedule;

    public ReportBase(int hourOfDay, int minuteOfHour) {
        this.schedule = dailyAtHourAndMinute(hourOfDay, minuteOfHour)
                .withMisfireHandlingInstructionFireAndProceed(); // On misfire, fire immediately, then back to schedule.
    }

    public ScheduleBuilder<? extends Trigger> getSchedule() {
        return schedule;
    }

    public abstract String getName();

    public abstract Class<? extends Job> getJobClass();

}
