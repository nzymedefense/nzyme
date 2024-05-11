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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.annotation.Nullable;
import org.joda.time.DateTime;

@AutoValue
public abstract class DNSLogReport {

    @Nullable
    public abstract Integer transactionId();
    public abstract String clientAddress();
    public abstract String serverAddress();
    public abstract String clientMac();
    public abstract String serverMac();
    public abstract int clientPort();
    public abstract int serverPort();
    public abstract String dataValue();
    public abstract String dataType();
    public abstract DateTime timestamp();

    @JsonCreator
    public static DNSLogReport create(@JsonProperty("transaction_id") Integer transactionId,
                                      @JsonProperty("client_address") String clientAddress,
                                      @JsonProperty("server_address") String serverAddress,
                                      @JsonProperty("client_mac") String clientMac,
                                      @JsonProperty("server_mac") String serverMac,
                                      @JsonProperty("client_port") int clientPort,
                                      @JsonProperty("server_port") int serverPort,
                                      @JsonProperty("data_value") String dataValue,
                                      @JsonProperty("data_type") String dataType,
                                      @JsonProperty("timestamp") DateTime timestamp) {
        return builder()
                .transactionId(transactionId)
                .clientAddress(clientAddress)
                .serverAddress(serverAddress)
                .clientMac(clientMac)
                .serverMac(serverMac)
                .clientPort(clientPort)
                .serverPort(serverPort)
                .dataValue(dataValue)
                .dataType(dataType)
                .timestamp(timestamp)
                .build();
    }

    public static DNSLogReport.Builder builder() {
        return new AutoValue_DNSLogReport.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract DNSLogReport.Builder transactionId(Integer transactionId);

        public abstract DNSLogReport.Builder clientAddress(String clientAddress);

        public abstract DNSLogReport.Builder serverAddress(String serverAddress);

        public abstract DNSLogReport.Builder clientMac(String clientMac);

        public abstract DNSLogReport.Builder serverMac(String serverMac);

        public abstract DNSLogReport.Builder clientPort(int clientPort);

        public abstract DNSLogReport.Builder serverPort(int serverPort);

        public abstract DNSLogReport.Builder dataValue(String dataValue);

        public abstract DNSLogReport.Builder dataType(String dataType);

        public abstract DNSLogReport.Builder timestamp(DateTime timestamp);

        public abstract DNSLogReport build();
    }
}
