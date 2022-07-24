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

package horse.wtf.nzyme.rest.resources.taps.reports.tables;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSNxDomainLogReport {

    public abstract String ip();
    public abstract String server();
    public abstract String queryValue();
    public abstract String dataType();
    public abstract DateTime timestamp();

    @JsonCreator
    public static DNSNxDomainLogReport create(@JsonProperty("ip") String ip,
                                              @JsonProperty("server") String server,
                                              @JsonProperty("query_value") String queryValue,
                                              @JsonProperty("data_type") String dataType,
                                              @JsonProperty("timestamp") DateTime timestamp) {
        return builder()
                .ip(ip)
                .server(server)
                .queryValue(queryValue)
                .dataType(dataType)
                .timestamp(timestamp)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSNxDomainLogReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ip(String ip);

        public abstract Builder server(String server);

        public abstract Builder queryValue(String queryValue);

        public abstract Builder dataType(String dataType);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract DNSNxDomainLogReport build();
    }

}
