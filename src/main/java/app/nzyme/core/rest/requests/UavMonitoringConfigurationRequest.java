package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class UavMonitoringConfigurationRequest {

    public abstract boolean alertOnUnknown();
    public abstract boolean alertOnFriendly();
    public abstract boolean alertOnNeutral();
    public abstract boolean alertOnHostile();

    @JsonCreator
    public static UavMonitoringConfigurationRequest create(@JsonProperty("alert_on_unknown") boolean alertOnUnknown,
                                                           @JsonProperty("alert_on_friendly") boolean alertOnFriendly,
                                                           @JsonProperty("alert_on_neutral") boolean alertOnNeutral,
                                                           @JsonProperty("alert_on_hostile") boolean alertOnHostile) {
        return builder()
                .alertOnUnknown(alertOnUnknown)
                .alertOnFriendly(alertOnFriendly)
                .alertOnNeutral(alertOnNeutral)
                .alertOnHostile(alertOnHostile)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_UavMonitoringConfigurationRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder alertOnUnknown(boolean alertOnUnknown);

        public abstract Builder alertOnFriendly(boolean alertOnFriendly);

        public abstract Builder alertOnNeutral(boolean alertOnNeutral);

        public abstract Builder alertOnHostile(boolean alertOnHostile);

        public abstract UavMonitoringConfigurationRequest build();
    }
}
