package app.nzyme.core.rest.responses.bluetooth.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class BluetoothMonitoringRulesListResponse {

    @JsonProperty("count")
    public abstract long count();

    @JsonProperty("rules")
    public abstract List<BluetoothMonitoringRuleDetailsResponse> rules();

    public static BluetoothMonitoringRulesListResponse create(long count, List<BluetoothMonitoringRuleDetailsResponse> rules) {
        return builder()
                .count(count)
                .rules(rules)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_BluetoothMonitoringRulesListResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder count(long count);

        public abstract Builder rules(List<BluetoothMonitoringRuleDetailsResponse> rules);

        public abstract BluetoothMonitoringRulesListResponse build();
    }
}
