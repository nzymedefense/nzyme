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

package app.nzyme.core.rest.responses.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import app.nzyme.core.rest.responses.alerts.AlertsListResponse;
import app.nzyme.core.rest.responses.bandits.ContactResponse;
import app.nzyme.core.rest.responses.system.ProbesListResponse;
import app.nzyme.core.systemstatus.SystemStatus;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

@AutoValue
public abstract class DashboardResponse {

    @JsonProperty("active_alerts")
    public abstract long activeAlerts();

    @JsonProperty("active_contacts")
    public abstract long activeContacts();

    @JsonProperty("system_health_status")
    public abstract SystemStatus.HEALTH systemHealthStatus();

    @JsonProperty("deauth_frame_histogram")
    public abstract Map<String, Long> deauthFrameHistogram();

    @JsonProperty("deauth_threshold")
    @Nullable
    public abstract Integer deauthThreshold();

    @JsonProperty("alerts")
    public abstract AlertsListResponse alerts();

    @JsonProperty("contacts")
    public abstract List<ContactResponse> contacts();

    @JsonProperty("probes")
    public abstract ProbesListResponse probes();

    public static DashboardResponse create(long activeAlerts, long activeContacts, SystemStatus.HEALTH systemHealthStatus, Map<String, Long> deauthFrameHistogram, Integer deauthThreshold, AlertsListResponse alerts, List<ContactResponse> contacts, ProbesListResponse probes) {
        return builder()
                .activeAlerts(activeAlerts)
                .activeContacts(activeContacts)
                .systemHealthStatus(systemHealthStatus)
                .deauthFrameHistogram(deauthFrameHistogram)
                .deauthThreshold(deauthThreshold)
                .alerts(alerts)
                .contacts(contacts)
                .probes(probes)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_DashboardResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder activeAlerts(long activeAlerts);

        public abstract Builder activeContacts(long activeContacts);

        public abstract Builder systemHealthStatus(SystemStatus.HEALTH systemHealthStatus);

        public abstract Builder deauthFrameHistogram(Map<String, Long> deauthFrameHistogram);

        public abstract Builder deauthThreshold(Integer deauthThreshold);

        public abstract Builder alerts(AlertsListResponse alerts);

        public abstract Builder contacts(List<ContactResponse> contacts);

        public abstract Builder probes(ProbesListResponse probes);

        public abstract DashboardResponse build();
    }
}
