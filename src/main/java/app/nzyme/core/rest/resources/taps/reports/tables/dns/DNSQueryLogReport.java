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

package app.nzyme.core.rest.resources.taps.reports.tables.dns;

import app.nzyme.core.rest.resources.taps.reports.tables.dns.AutoValue_DNSQueryLogReport;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSQueryLogReport {

    public abstract String ip();

    public abstract String server();
    public abstract String sourceMac();
    public abstract String destinationMac();
    public abstract Long port();
    public abstract String queryValue();
    public abstract String dataType();
    public abstract DateTime timestamp();

    @JsonCreator
    public static DNSQueryLogReport create(@JsonProperty("ip") String ip,
                                           @JsonProperty("server") String server,
                                           @JsonProperty("source_mac") String sourceMac,
                                           @JsonProperty("destination_mac") String destinationMac,
                                           @JsonProperty("port") Long port,
                                           @JsonProperty("query_value") String queryValue,
                                           @JsonProperty("data_type") String dataType,
                                           @JsonProperty("timestamp") DateTime timestamp) {
        return builder()
                .ip(ip)
                .server(server)
                .sourceMac(sourceMac)
                .destinationMac(destinationMac)
                .port(port)
                .queryValue(queryValue)
                .dataType(dataType)
                .timestamp(timestamp)
                .build();
    }

    public static DNSQueryLogReport.Builder builder() {
        return new AutoValue_DNSQueryLogReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract DNSQueryLogReport.Builder ip(String ip);

        public abstract DNSQueryLogReport.Builder server(String server);

        public abstract DNSQueryLogReport.Builder sourceMac(String sourceMac);

        public abstract DNSQueryLogReport.Builder destinationMac(String destinationMac);

        public abstract DNSQueryLogReport.Builder port(Long port);

        public abstract DNSQueryLogReport.Builder queryValue(String queryValue);

        public abstract DNSQueryLogReport.Builder dataType(String dataType);

        public abstract DNSQueryLogReport.Builder timestamp(DateTime timestamp);

        public abstract DNSQueryLogReport build();
    }
}
