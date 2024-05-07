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

package app.nzyme.core.rest.responses.ethernet.dns;

import app.nzyme.core.rest.responses.shared.GeoInformationResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DNSPairSummaryResponse {

    @JsonProperty("server")
    public abstract String server();

    @JsonProperty("server_geo")
    public abstract GeoInformationResponse geo();

    @JsonProperty("request_count")
    public abstract Long requestCount();

    @JsonProperty("client_count")
    public abstract Long clientCount();

    public static DNSPairSummaryResponse create(String server, GeoInformationResponse geo, Long requestCount, Long clientCount) {
        return builder()
                .server(server)
                .geo(geo)
                .requestCount(requestCount)
                .clientCount(clientCount)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DNSPairSummaryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder server(String server);

        public abstract Builder geo(GeoInformationResponse geo);

        public abstract Builder requestCount(Long requestCount);

        public abstract Builder clientCount(Long clientCount);

        public abstract DNSPairSummaryResponse build();
    }
}
