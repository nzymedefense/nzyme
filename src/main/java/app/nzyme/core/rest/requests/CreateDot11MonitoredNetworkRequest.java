package app.nzyme.core.rest.requests;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import jakarta.validation.constraints.NotEmpty;

@AutoValue
public abstract class CreateDot11MonitoredNetworkRequest {

    @NotEmpty
    public abstract String ssid();

    @JsonCreator
    public static CreateDot11MonitoredNetworkRequest create(@JsonProperty("ssid") String ssid) {
        return builder()
                .ssid(ssid)
                .build();
    }

    public static Builder builder() {
        return new AutoValue_CreateDot11MonitoredNetworkRequest.Builder();
    }

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder ssid(String ssid);

        public abstract CreateDot11MonitoredNetworkRequest build();
    }
}
