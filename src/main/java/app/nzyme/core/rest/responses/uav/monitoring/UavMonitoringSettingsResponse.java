package app.nzyme.core.rest.responses.uav.monitoring;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UavMonitoringSettingsResponse {

    @JsonProperty("alert_on_unknown")
    public abstract boolean alertOnUnknown();
    @JsonProperty("alert_on_friendly")
    public abstract boolean alertOnFriendly();
    @JsonProperty("alert_on_neutral")
    public abstract boolean alertOnNeutral();
    @JsonProperty("alert_on_hostile")
    public abstract boolean alertOnHostile();

    public static UavMonitoringSettingsResponse create(boolean alertOnUnknown, boolean alertOnFriendly, boolean alertOnNeutral, boolean alertOnHostile) {
        return builder()
                .alertOnUnknown(alertOnUnknown)
                .alertOnFriendly(alertOnFriendly)
                .alertOnNeutral(alertOnNeutral)
                .alertOnHostile(alertOnHostile)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavMonitoringSettingsResponse.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder alertOnUnknown(boolean alertOnUnknown);

        public abstract Builder alertOnFriendly(boolean alertOnFriendly);

        public abstract Builder alertOnNeutral(boolean alertOnNeutral);

        public abstract Builder alertOnHostile(boolean alertOnHostile);

        public abstract UavMonitoringSettingsResponse build();
    }

}
