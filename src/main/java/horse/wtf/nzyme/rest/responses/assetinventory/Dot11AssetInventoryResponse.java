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

package horse.wtf.nzyme.rest.responses.assetinventory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Dot11AssetInventoryResponse {

    @JsonProperty
    public abstract List<Dot11SSIDAssetResponse> ssids();

    @JsonProperty("ssids_csv")
    public abstract String ssidsCSV();

    @JsonProperty("bssids_csv")
    public abstract String bssidsCSV();

    public static Dot11AssetInventoryResponse create(List<Dot11SSIDAssetResponse> ssids, String ssidsCSV, String bssidsCSV) {
        return builder()
                .ssids(ssids)
                .ssidsCSV(ssidsCSV)
                .bssidsCSV(bssidsCSV)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_Dot11AssetInventoryResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssids(List<Dot11SSIDAssetResponse> ssids);

        public abstract Builder ssidsCSV(String ssidsCSV);

        public abstract Builder bssidsCSV(String bssidsCSV);

        public abstract Dot11AssetInventoryResponse build();
    }

}
