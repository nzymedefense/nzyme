/*
 *  This file is part of nzyme.
 *
 *  nzyme is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  nzyme is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with nzyme.  If not, see <http://www.gnu.org/licenses/>.
 */

package horse.wtf.nzyme.rest.responses.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.Map;

@AutoValue
public abstract class MetricsListResponse {

    @JsonProperty("total")
    public abstract int total();

    @JsonProperty("metrics")
    public abstract Map<String, Object> metrics();

    public static MetricsListResponse create(int total, Map<String, Object> metrics) {
        return builder()
                .total(total)
                .metrics(metrics)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_MetricsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(int total);

        public abstract Builder metrics(Map<String, Object> metrics);

        public abstract MetricsListResponse build();
    }

}
