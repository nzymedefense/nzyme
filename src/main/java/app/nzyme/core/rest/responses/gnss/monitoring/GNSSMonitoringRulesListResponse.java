package app.nzyme.core.rest.responses.gnss.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class GNSSMonitoringRulesListResponse {

    @JsonProperty("total")
    public abstract long total();

    @JsonProperty("rules")
    public abstract List<GNSSMonitoringRuleDetailsResponse> rules();

    public static GNSSMonitoringRulesListResponse create(long total, List<GNSSMonitoringRuleDetailsResponse> rules) {
        return builder()
                .total(total)
                .rules(rules)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_GNSSMonitoringRulesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder total(long total);

        public abstract Builder rules(List<GNSSMonitoringRuleDetailsResponse> rules);

        public abstract GNSSMonitoringRulesListResponse build();
    }
}
