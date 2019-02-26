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

package horse.wtf.nzyme.rest.responses.system;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class ProbesListResponse {

    @JsonProperty("total")
    public abstract int total();

    @JsonProperty("probes")
    public abstract List<ProbeResponse> probes();

    public static ProbesListResponse create(int total, List<ProbeResponse> probes) {
        return builder()
                .total(total)
                .probes(probes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_ProbesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(int total);

        public abstract Builder probes(List<ProbeResponse> probes);

        public abstract ProbesListResponse build();
    }

}
