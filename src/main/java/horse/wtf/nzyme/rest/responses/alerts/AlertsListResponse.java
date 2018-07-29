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

package horse.wtf.nzyme.rest.responses.alerts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class AlertsListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("alerts")
    public abstract List<AlertDetailsResponse> alerts();

    public static AlertsListResponse create(long total, List<AlertDetailsResponse> alerts) {
        return builder()
                .total(total)
                .alerts(alerts)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_AlertsListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder alerts(List<AlertDetailsResponse> alerts);

        public abstract AlertsListResponse build();
    }

}
