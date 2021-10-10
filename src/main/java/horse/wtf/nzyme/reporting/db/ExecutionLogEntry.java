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

package horse.wtf.nzyme.reporting.db;

import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class ExecutionLogEntry {

    public abstract String reportName();
    public abstract String result();
    public abstract String message();
    public abstract DateTime createdAt();

    public static ExecutionLogEntry create(String reportName, String result, String message, DateTime createdAt) {
        return builder()
                .reportName(reportName)
                .result(result)
                .message(message)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ExecutionLogEntry.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder reportName(String reportName);

        public abstract Builder result(String result);

        public abstract Builder message(String message);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract ExecutionLogEntry build();
    }

}
