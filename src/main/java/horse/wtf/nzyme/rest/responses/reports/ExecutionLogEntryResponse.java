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

@AutoValue
public abstract class ExecutionLogEntryResponse {

    @JsonProperty("result")
    public abstract String result();

    @JsonProperty("message")
    public abstract String message();

    @JsonProperty("created_at")
    public abstract DateTime createdAt();

    public static ExecutionLogEntryResponse create(String result, String message, DateTime createdAt) {
        return builder()
                .result(result)
                .message(message)
                .createdAt(createdAt)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ExecutionLogEntryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder result(String result);

        public abstract Builder message(String message);

        public abstract Builder createdAt(DateTime createdAt);

        public abstract ExecutionLogEntryResponse build();
    }

}
