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

package horse.wtf.nzyme.rest.responses.networks;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import horse.wtf.nzyme.dot11.networks.BSSID;

import java.util.*;

@AutoValue
public abstract class BSSIDsResponse {

    @JsonProperty
    public abstract int total();

    @JsonProperty
    public abstract List<BSSIDResponse> bssids();

    public static BSSIDsResponse create(int total, List<BSSIDResponse> bssids) {
        Collections.sort(bssids, new BSSIDComparator());

        return builder()
                .total(total)
                .bssids(bssids)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BSSIDsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(int total);

        public abstract Builder bssids(List<BSSIDResponse> bssids);

        public abstract BSSIDsResponse build();
    }

    private static class BSSIDComparator implements Comparator<BSSIDResponse> {

        @Override
        public int compare(BSSIDResponse b1, BSSIDResponse b2) {
            return b2.signalStrength() - b1.signalStrength();
        }

    }

}
