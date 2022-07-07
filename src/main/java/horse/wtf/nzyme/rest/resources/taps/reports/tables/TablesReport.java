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

import java.util.Map;

@AutoValue
public abstract class TablesReport {

    public abstract String tapName();
    public abstract DateTime timestamp();
    public abstract Map<String, Map<String, Long>> arp();

    public abstract DNSTablesReport dns();

    @JsonCreator
    public static TablesReport create(@JsonProperty("tap_name") String tapName,
                                      @JsonProperty("timestamp") DateTime timestamp,
                                      @JsonProperty("arp") Map<String, Map<String, Long>> arp,
                                      @JsonProperty("dns") DNSTablesReport dns) {
        return builder()
                .tapName(tapName)
                .timestamp(timestamp)
                .arp(arp)
                .dns(dns)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_TablesReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder tapName(String tapName);

        public abstract Builder timestamp(DateTime timestamp);

        public abstract Builder arp(Map<String, Map<String, Long>> arp);

        public abstract Builder dns(DNSTablesReport dns);

        public abstract TablesReport build();
    }

}
