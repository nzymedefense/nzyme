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

package app.nzyme.core.ethernet.dns.db;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DNSTrafficSummary {

    public abstract long totalPackets();
    public abstract long totalTrafficBytes();
    public abstract long totalNxdomains();

    public static DNSTrafficSummary create(long totalPackets, long totalTrafficBytes, long totalNxdomains) {
        return builder()
                .totalPackets(totalPackets)
                .totalTrafficBytes(totalTrafficBytes)
                .totalNxdomains(totalNxdomains)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSTrafficSummary.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder totalPackets(long totalPackets);

        public abstract Builder totalTrafficBytes(long totalTrafficBytes);

        public abstract Builder totalNxdomains(long totalNxdomains);

        public abstract DNSTrafficSummary build();
    }

}
