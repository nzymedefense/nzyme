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

package app.nzyme.core.rest.responses.assetinventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Dot11SSIDAssetResponse {

    @JsonProperty
    public abstract String ssid();

    @JsonProperty
    public abstract List<Dot11BSSIDAssetResponse> bssids();

    @JsonProperty
    public abstract List<Integer> channels();

    @JsonProperty
    public abstract List<String> security();

    public static Dot11SSIDAssetResponse create(String ssid, List<Dot11BSSIDAssetResponse> bssids, List<Integer> channels, List<String> security) {
        return builder()
                .ssid(ssid)
                .bssids(bssids)
                .channels(channels)
                .security(security)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11SSIDAssetResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract Builder bssids(List<Dot11BSSIDAssetResponse> bssids);

        public abstract Builder channels(List<Integer> channels);

        public abstract Builder security(List<String> security);

        public abstract Dot11SSIDAssetResponse build();
    }

}
