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

package horse.wtf.nzyme.rest.resources.taps.reports.tables.retro;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSRetroQueryLogReport {

    public abstract String ip();

    public abstract String server();
    public abstract String sourceMac();
    public abstract String destinationMac();
    public abstract Long port();
    public abstract String queryValue();
    public abstract String dataType();
    public abstract DateTime timestamp();

    @JsonCreator
    public static DNSRetroQueryLogReport create(@JsonProperty("ip") String ip,
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

    public static DNSRetroQueryLogReport.Builder builder() {
        return new AutoValue_DNSRetroQueryLogReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract DNSRetroQueryLogReport.Builder ip(String ip);

        public abstract DNSRetroQueryLogReport.Builder server(String server);

        public abstract DNSRetroQueryLogReport.Builder sourceMac(String sourceMac);

        public abstract DNSRetroQueryLogReport.Builder destinationMac(String destinationMac);

        public abstract DNSRetroQueryLogReport.Builder port(Long port);

        public abstract DNSRetroQueryLogReport.Builder queryValue(String queryValue);

        public abstract DNSRetroQueryLogReport.Builder dataType(String dataType);

        public abstract DNSRetroQueryLogReport.Builder timestamp(DateTime timestamp);

        public abstract DNSRetroQueryLogReport build();
    }
}
